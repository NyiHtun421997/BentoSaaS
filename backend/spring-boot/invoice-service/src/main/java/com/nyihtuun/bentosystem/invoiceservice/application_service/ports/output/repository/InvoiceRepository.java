package com.nyihtuun.bentosystem.invoiceservice.application_service.ports.output.repository;

import com.nyihtuun.bentosystem.invoiceservice.domain.entity.Invoice;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository {
    Optional<Invoice> findByInvoiceId(UUID invoiceId);
    List<Invoice> findByUserIdAndDate(UUID userId, LocalDate date);
    Invoice save(Invoice invoice);
}
