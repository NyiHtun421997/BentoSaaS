package com.nyihtuun.bentosystem.invoiceservice.data_access.jpa_repository;

import com.nyihtuun.bentosystem.invoiceservice.data_access.jpa_entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, String> {
    Optional<PaymentEntity> findByIdempotencyKey(UUID idempotencyKey);

    Optional<PaymentEntity> findByInvoiceIdAndPaymentStatus(UUID invoiceId, String paymentStatus);
}
