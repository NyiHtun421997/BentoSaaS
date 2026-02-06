package com.nyihtuun.bentosystem.invoiceservice.controller;

import com.nyihtuun.bentosystem.invoiceservice.application_service.dto.response.InvoiceResponseDto;
import com.nyihtuun.bentosystem.invoiceservice.application_service.ports.input.service.InvoiceService;
import com.nyihtuun.bentosystem.invoiceservice.domain.exception.InvoiceDomainException;
import com.nyihtuun.bentosystem.invoiceservice.domain.exception.InvoiceErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.nyihtuun.bentosystem.invoiceservice.controller.ApiPaths.INVOICE_ID;
import static com.nyihtuun.bentosystem.invoiceservice.controller.ApiPaths.VERSION1;

@Slf4j
@RestController
@RequestMapping(VERSION1)
@Tag(name = "Invoice", description = "Endpoints for managing invoices and payments.")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
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

    @PutMapping("/pay" + INVOICE_ID)
    @Operation(summary = "Make payment", description = "Processes payment for an invoice.")
    @ApiResponse(responseCode = "200", description = "Payment processed successfully")
    public ResponseEntity<InvoiceResponseDto> makePayment(@PathVariable UUID invoiceId) {
        log.info("Making payment for invoice with id: {}", invoiceId);
        return ResponseEntity.ok(invoiceService.makePayment(invoiceId));
    }

    @PutMapping("/cancel" + INVOICE_ID)
    @Operation(summary = "Cancel payment", description = "Cancels a payment for an invoice.")
    @ApiResponse(responseCode = "200", description = "Payment cancelled successfully")
    public ResponseEntity<InvoiceResponseDto> cancelPayment(@PathVariable UUID invoiceId) {
        log.info("Cancelling payment for invoice with id: {}", invoiceId);
        return ResponseEntity.ok(invoiceService.cancelPayment(invoiceId));
    }

    @PutMapping("/fail" + INVOICE_ID)
    @Operation(summary = "Mark payment failed", description = "Marks an invoice payment as failed.")
    @ApiResponse(responseCode = "200", description = "Payment marked as failed successfully")
    public ResponseEntity<InvoiceResponseDto> markPaymentFailed(@PathVariable UUID invoiceId) {
        log.info("Marking payment for invoice with id: {} as failed", invoiceId);
        return ResponseEntity.ok(invoiceService.markPaymentFailed(invoiceId));
    }
}
