package com.nyihtuun.bentosystem.planmanagementservice.application_service.outbox.model;

import com.google.protobuf.Message;
import com.nyihtuun.bentosystem.domain.messaging.BaseMessagingModel;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@ToString
public class PlanOutboxMessage extends BaseMessagingModel {
    private Message payload;
}
