package com.nyihtuun.bentosystem.subscriptionservice.data_access.jpa_entity;

import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Table(name = "user_plan_subscription_event_outbox", schema = "subscription")
@Entity
public class UserPlanSubscriptionEventOutboxEntity {
    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant processedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "outbox_status", nullable = false)
    private OutboxStatus outboxStatus;

    @Version
    private int version;
}
