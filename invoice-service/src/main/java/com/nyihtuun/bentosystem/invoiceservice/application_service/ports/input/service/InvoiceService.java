package com.nyihtuun.bentosystem.invoiceservice.application_service.ports.input.service;

import com.nyihtuun.bentosystem.invoiceservice.domain.entity.Invoice;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceService {
    Optional<Invoice> getInvoiceById(UUID invoiceId);
    List<Invoice> getMyInvoicesByDate(UUID userId, LocalDate date);
    Invoice makePayment(UUID invoiceId);
    Invoice cancelPayment(UUID invoiceId);
    Invoice markPaymentFailed(UUID invoiceId);
}
