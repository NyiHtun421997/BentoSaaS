package com.nyihtuun.bentosystem.invoiceservice.data_access.jpa_repository;

import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import com.nyihtuun.bentosystem.invoiceservice.data_access.jpa_entity.InvoiceEventOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceEventOutboxJpaRepository extends JpaRepository<InvoiceEventOutboxEntity, UUID> {
    Optional<List<InvoiceEventOutboxEntity>> findByOutboxStatus(OutboxStatus outboxStatus);
    void deleteByOutboxStatus(OutboxStatus outboxStatus);
}
