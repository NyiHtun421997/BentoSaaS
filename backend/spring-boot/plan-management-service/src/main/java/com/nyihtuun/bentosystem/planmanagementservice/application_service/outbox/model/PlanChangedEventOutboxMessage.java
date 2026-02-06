package com.nyihtuun.bentosystem.planmanagementservice.application_service.outbox.model;

import com.nyihtuun.bentosystem.domain.messaging.BaseMessagingModel;
import lombok.*;
import lombok.experimental.SuperBuilder;
import plan_management.events.PlanChangedEvent;

@Getter
@SuperBuilder
@NoArgsConstructor
@ToString
public class PlanChangedEventOutboxMessage extends BaseMessagingModel {
    private PlanChangedEvent payload;
}
