package com.nyihtuun.bentosystem.domain.messaging;

import com.google.protobuf.Message;
import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public abstract class BaseMessagingModel {
    @EqualsAndHashCode.Include
    private UUID id;
    private Instant createdAt;

    @Setter
    private Instant processedAt;

    @Setter
    private OutboxStatus outboxStatus;
    private int version;

   protected abstract Message getPayload();
}
