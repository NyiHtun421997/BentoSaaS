package com.nyihtuun.bentosystem.invoiceservice.application_service.ports.input.service;

import com.nyihtuun.bentosystem.invoiceservice.application_service.dto.response.InvoiceResponseDto;

import java.util.Optional;
import java.util.UUID;

public interface PaymentService {
    Optional<String> createPayment(InvoiceResponseDto invoiceResponseDto, String idempotencyKey);
    boolean updatePaymentStatus(String paymentIntentId, String paymentStatus);
    boolean cancelPayment(UUID invoiceId);
}
