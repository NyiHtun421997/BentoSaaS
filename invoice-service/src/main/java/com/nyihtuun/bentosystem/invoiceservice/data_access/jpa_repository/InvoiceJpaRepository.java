package com.nyihtuun.bentosystem.invoiceservice.data_access.jpa_repository;

import com.nyihtuun.bentosystem.invoiceservice.data_access.jpa_entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface InvoiceJpaRepository extends JpaRepository<InvoiceEntity, UUID> {
    List<InvoiceEntity> findAllByUserIdAndIssuedAtAfter(UUID userId, LocalDateTime dateTime);
}
