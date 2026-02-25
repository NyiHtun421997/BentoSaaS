package com.nyihtuun.bentosystem.invoiceservice.application_service.impl.service;

import com.nyihtuun.bentosystem.domain.valueobject.status.InvoiceStatus;
import com.nyihtuun.bentosystem.invoiceservice.application_service.dto.response.InvoiceResponseDto;
import com.nyihtuun.bentosystem.invoiceservice.application_service.ports.input.service.InvoiceService;
import com.nyihtuun.bentosystem.invoiceservice.application_service.ports.input.service.PaymentService;
import com.nyihtuun.bentosystem.invoiceservice.data_access.jpa_entity.PaymentEntity;
import com.nyihtuun.bentosystem.invoiceservice.data_access.jpa_repository.PaymentJpaRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    public static final String JPY = "JPY";
    public static final String INTENT = "-intent";
    private final PaymentJpaRepository paymentJpaRepository;
    private final InvoiceService invoiceService;

    public PaymentServiceImpl(PaymentJpaRepository paymentJpaRepository, @Value("${stripe.secret.key}") String stripeSecretKey,
                              InvoiceService invoiceService) {
        this.paymentJpaRepository = paymentJpaRepository;
        this.invoiceService = invoiceService;
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    @Transactional
    public Optional<String> createPayment(InvoiceResponseDto invoiceResponseDto, String idempotencyKey) {
        // check payment with the same key
        UUID idempotencyKeyUUID = UUID.fromString(idempotencyKey);
        if (paymentJpaRepository.findByIdempotencyKey(idempotencyKeyUUID).isPresent()) {
            log.warn("Payment intent already exists for idempotency key: {}", idempotencyKey);
            return Optional.empty();
        }

        // Prepare endpoint-specific key: avoids collisions with checkout sessions
        String intentKey = !idempotencyKey.isBlank()
                ? idempotencyKey + INTENT
                : UUID.randomUUID().toString() + INTENT;

        // create payment intent
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                                                                    .setAmount(invoiceResponseDto.getAmount().longValueExact())
                                                                    .setCurrency(JPY)
                                                                    .setAutomaticPaymentMethods(
                                                                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                                                                                                             .setEnabled(
                                                                                                                                     true)
                                                                                                                             .build()
                                                                    )
                                                                    .build();

        RequestOptions requestOptions = RequestOptions.builder()
                                                      .setIdempotencyKey(intentKey)
                                                      .build();

        PaymentIntent paymentIntent = null;
        try {
            paymentIntent = PaymentIntent.create(params, requestOptions);
        } catch (StripeException e) {
            log.error("Stripe payment intent creation failed: {}", e.getMessage());
            return Optional.empty();
        }

        // store payment
        PaymentEntity paymentEntity = PaymentEntity.builder()
                                                   .id(paymentIntent.getId())
                                                   .invoiceId(invoiceResponseDto.getInvoiceId())
                                                   .paymentStatus("PENDING")
                                                   .amount(invoiceResponseDto.getAmount())
                                                   .currency(JPY)
                                                   .idempotencyKey(idempotencyKeyUUID)
                                                   .createdAt(Instant.now())
                                                   .build();
        paymentJpaRepository.save(paymentEntity);
        log.info("Payment intent created and persisted successfully.");

        return Optional.of(paymentIntent.getClientSecret());
    }

    @Override
    @Transactional
    public boolean updatePaymentStatus(String paymentIntentId, String paymentStatus) {
        return paymentJpaRepository.findById(paymentIntentId).map(
                                           paymentEntity -> {
                                               paymentEntity.setPaymentStatus(paymentStatus);
                                               paymentEntity.setUpdatedAt(Instant.now());
                                               paymentJpaRepository.save(paymentEntity);
                                               log.info("Payment status of id: {} updated successfully to {}.", paymentIntentId, paymentStatus);

                                               final InvoiceStatus invoiceStatus;
                                               switch (paymentStatus) {
                                                   case "SUCCESS" -> {
                                                       invoiceStatus = InvoiceStatus.PAID;
                                                       invoiceService.makePayment(paymentEntity.getInvoiceId());
                                                   }
                                                   case "REFUNDED" -> {
                                                       invoiceStatus = InvoiceStatus.CANCELLED;
                                                       invoiceService.updateInvoiceStatus(paymentEntity.getInvoiceId(), invoiceStatus);
                                                   }
                                                   case "FAILED" -> {
                                                       invoiceStatus = InvoiceStatus.FAILED;
                                                       invoiceService.updateInvoiceStatus(paymentEntity.getInvoiceId(), invoiceStatus);
                                                   }
                                                   default -> {
                                                       log.warn("Payment status {} is not supported.", paymentStatus);
                                                       return false;
                                                   }
                                               }

                                               log.info("Invoice status of id: {} updated successfully to {}.", paymentEntity.getInvoiceId(), invoiceStatus);
                                               return true;
                                           })
                                   .orElseGet(() -> {
                                       log.error("Payment with id: {} not found. Couldn't update payment.", paymentIntentId);
                                       return false;
                                   });
    }

    @Override
    @Transactional
    public boolean cancelPayment(UUID invoiceId) {
        // find payment associated with invoice id
        return paymentJpaRepository.findByInvoiceIdAndPaymentStatus(invoiceId, "SUCCESS")
                                   .map(paymentEntity -> {
                                       // check invoice status, if CANCELLED, return 400
                                       if (paymentEntity.getPaymentStatus().equals(InvoiceStatus.CANCELLED.name()))
                                           return false;

                                       // if PAID, do the following:
                                       // should check still cancellable (cancellable within 5 days of invoice creation)
                                       // else call stripe for refund
                                       if (Instant.now().isAfter(paymentEntity.getCreatedAt().plus(5, ChronoUnit.DAYS)))
                                           return false;

                                       RefundCreateParams params =
                                               RefundCreateParams.builder()
                                                                 .setPaymentIntent(paymentEntity.getId())
                                                                 .build();

                                       try {
                                           Refund refund = Refund.create(params);
                                           log.info("Refund of id: {} created successfully for payment id: {}.",
                                                    refund.getId(),
                                                    paymentEntity.getId());
                                           return true;
                                       } catch (StripeException e) {
                                           log.error("Stripe refund creation failed: {}", e.getMessage());
                                           return false;
                                       }
                                   })
                                   .orElseGet(() -> {
                                       log.error("Payment associated with invoice id: {} not found. Couldn't cancel payment.",
                                                 invoiceId);
                                       return false;
                                   });


    }
}
