package com.nyihtuun.bentosystem.subscriptionservice.application_service.outbox.model;

import com.nyihtuun.bentosystem.domain.messaging.BaseMessagingModel;
import lombok.*;
import lombok.experimental.SuperBuilder;
import subscription.events.UserPlanSubscriptionEvent;

@Getter
@SuperBuilder
@NoArgsConstructor
@ToString
public class UserPlanSubscriptionEventOutboxMessage extends BaseMessagingModel {
    private UserPlanSubscriptionEvent payload;
}
