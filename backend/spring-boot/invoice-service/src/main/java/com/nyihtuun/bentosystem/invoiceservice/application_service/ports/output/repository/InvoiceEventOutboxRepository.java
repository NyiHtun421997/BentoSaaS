package com.nyihtuun.bentosystem.invoiceservice.application_service.ports.output.repository;

import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import com.nyihtuun.bentosystem.invoiceservice.application_service.outbox.model.InvoiceEventOutboxMessage;

import java.util.List;
import java.util.Optional;

public interface InvoiceEventOutboxRepository {
    InvoiceEventOutboxMessage save(InvoiceEventOutboxMessage invoiceEventOutboxMessage);
    Optional<List<InvoiceEventOutboxMessage>> findByOutboxStatus(OutboxStatus status);
    void deleteByOutboxStatus(OutboxStatus status);
}
