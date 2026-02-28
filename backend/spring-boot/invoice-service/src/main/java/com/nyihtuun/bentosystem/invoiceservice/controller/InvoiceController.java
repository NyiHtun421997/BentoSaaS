package com.nyihtuun.bentosystem.invoiceservice.controller;

import com.nyihtuun.bentosystem.domain.valueobject.status.InvoiceStatus;
import com.nyihtuun.bentosystem.invoiceservice.application_service.dto.response.InvoiceResponseDto;
import com.nyihtuun.bentosystem.invoiceservice.application_service.ports.input.service.InvoiceService;
import com.nyihtuun.bentosystem.invoiceservice.application_service.ports.input.service.PaymentService;
import com.nyihtuun.bentosystem.invoiceservice.application_service.scheduler.InvoiceGenerationScheduler;
import com.nyihtuun.bentosystem.invoiceservice.domain.exception.InvoiceDomainException;
import com.nyihtuun.bentosystem.invoiceservice.domain.exception.InvoiceErrorCode;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.nyihtuun.bentosystem.invoiceservice.InvoiceConstants.IDEMPOTENCY_KEY;
import static com.nyihtuun.bentosystem.invoiceservice.InvoiceConstants.STRIPE_SIGNATURE;
import static com.nyihtuun.bentosystem.invoiceservice.controller.ApiPaths.INVOICE_ID;
import static com.nyihtuun.bentosystem.invoiceservice.controller.ApiPaths.VERSION1;

@Slf4j
@RestController
@RequestMapping(VERSION1)
@Tag(name = "Invoice", description = "Endpoints for managing invoices and payments.")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final String webhookSecret;
    private final InvoiceGenerationScheduler scheduler;

    @Autowired
    public InvoiceController(InvoiceService invoiceService,
                             PaymentService paymentService,
                             @Value("${stripe.webhook.secret}") String webhookSecret, InvoiceGenerationScheduler scheduler) {
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
        this.webhookSecret = webhookSecret;
        this.scheduler = scheduler;
    }

    @GetMapping(INVOICE_ID)
    @Operation(summary = "Get invoice by ID", description = "Retrieves an invoice by its unique identifier.")
    @ApiResponse(responseCode = "200", description = "Invoice retrieved successfully")
    public ResponseEntity<InvoiceResponseDto> findInvoiceById(@PathVariable UUID invoiceId) {
        log.info("Fetching invoice with id: {}", invoiceId);
        return invoiceService.getInvoiceById(invoiceId)
                             .map(invoice -> {
                                 log.info("Invoice with id: {} : {}", invoiceId, invoice);
                                 return ResponseEntity.ok(invoice);
                             })
                             .orElseThrow(() -> {
                                 log.error("Invoice with id: {} not found", invoiceId);
                                 return new InvoiceDomainException(InvoiceErrorCode.INVOICE_NOT_FOUND);
                             });
    }

    @GetMapping("byuseridanddate")
    @Operation(summary = "Find invoices by user ID and date", description = "Retrieves a list of invoices for a specific user on a given date.")
    @ApiResponse(responseCode = "200", description = "Invoices retrieved successfully")
    public ResponseEntity<List<InvoiceResponseDto>> findInvoicesByUserIdAndDate(
            @RequestParam UUID userId,
            @RequestParam LocalDate date) {
        log.info("Fetching invoices for user with id: {} on date: {}", userId, date);
        List<InvoiceResponseDto> invoices = invoiceService.getMyInvoicesByDate(userId, date);
        log.info("Found {} invoices for user with id: {} on date: {}", invoices.size(), userId, date);
        return ResponseEntity.ok(invoices);
    }

    @PostMapping(INVOICE_ID + "/pay")
    @Operation(summary = "Make payment", description = "Processes payment for an invoice.")
    @ApiResponse(responseCode = "200", description = "Payment processed successfully")
    public ResponseEntity<String> makePayment(@PathVariable UUID invoiceId,
                                              @RequestHeader(IDEMPOTENCY_KEY) String idempotencyKey) {
        log.info("Making payment for invoice with id: {}", invoiceId);

        InvoiceResponseDto invoiceResponseDto = invoiceService.getInvoiceById(invoiceId).orElseThrow(() -> {
            log.error("Invoice with id: {} not found. Can't continue payment.", invoiceId);
            return new InvoiceDomainException(InvoiceErrorCode.INVOICE_NOT_FOUND);
        });
        // check invoice status, if PAID, return 400
        if (invoiceResponseDto.getInvoiceStatus() == InvoiceStatus.PAID) {
            log.error("Invoice with id: {} is already paid. Can't continue payment.", invoiceId);
            return ResponseEntity.badRequest().build();
        }
        // create payment intent
        return paymentService.createPayment(invoiceResponseDto, idempotencyKey)
                             .map(clientSecret -> {
                                 log.info("Payment intent created successfully. Client secret: {}", clientSecret);
                                 return ResponseEntity.ok(clientSecret);
                             })
                             .orElseGet(() -> {
                                 log.error("Payment intent creation failed. Can't continue payment.");
                                 return ResponseEntity.internalServerError().build();
                             });
    }

    @PostMapping("/webhook/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(STRIPE_SIGNATURE) String sigHeader) {
        log.info("Received Stripe webhook with payload: {}", payload);
        final Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            log.info("Stripe webhook event constructed successfully.");
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("");
        } catch (Exception e) {
            log.warn("Stripe webhook error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("");
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (intent != null) {
                    log.info("Updating status of payment with id: {} to SUCCESS", intent.getId());
                    return paymentService.updatePaymentStatus(intent.getId(), "SUCCESS")
                            ? ResponseEntity.ok("")
                            : ResponseEntity.internalServerError().build();
                }
            }
            case "payment_intent.payment_failed" -> {
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (intent != null) {
                    return paymentService.updatePaymentStatus(intent.getId(), "FAILED")
                            ? ResponseEntity.ok("")
                            : ResponseEntity.internalServerError().build();
                }
            }
            case "charge.refund.updated" -> {
                Refund refund = (Refund) event.getDataObjectDeserializer().getObject().orElse(null);
                if (refund != null) {
                    return paymentService.updatePaymentStatus(refund.getPaymentIntent(), "REFUNDED")
                            ? ResponseEntity.ok("")
                            : ResponseEntity.internalServerError().build();
                }
            }
            case null, default ->
                // add more events later as needed (refunds, disputes, etc.)
                    log.debug("Unhandled Stripe event type: {}", event.getType());
        }

        // Always return 200 for successfully verified events (even if unhandled)
        return ResponseEntity.ok("");
    }

    @PutMapping(INVOICE_ID + "/cancel")
    @Operation(summary = "Cancel payment", description = "Cancels a payment for an invoice.")
    @ApiResponse(responseCode = "200", description = "Payment cancelled successfully")
    public ResponseEntity<Void> cancelPayment(@PathVariable UUID invoiceId) {
        log.info("Cancelling payment for invoice with id: {}", invoiceId);

        return paymentService.cancelPayment(invoiceId)
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    @GetMapping("/generate")
    @Operation(summary = "Generate invoice", description = "Generates invoices.")
    @ApiResponse(responseCode = "200", description = "Invoices generated successfully")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> generateInvoice() throws JobInstanceAlreadyCompleteException, InvalidJobParametersException, JobExecutionAlreadyRunningException, JobRestartException {
        log.info("Generating invoices...");
        scheduler.process();
        return ResponseEntity.ok().build();
    }
}
