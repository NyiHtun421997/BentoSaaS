package com.nyihtuun.bentosystem.invoiceservice.data_access.jpa_entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "payment",
        schema = "invoice"
)
public class PaymentEntity {
    @Id
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private UUID idempotencyKey;

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "payment_ref", length = 255)
    private String providerRef;
}
