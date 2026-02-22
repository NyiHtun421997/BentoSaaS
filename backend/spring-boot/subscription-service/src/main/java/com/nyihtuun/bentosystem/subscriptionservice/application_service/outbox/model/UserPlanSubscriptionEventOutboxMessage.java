package com.nyihtuun.bentosystem.subscriptionservice.application_service.outbox.model;

import com.google.protobuf.Message;
import com.nyihtuun.bentosystem.domain.messaging.BaseMessagingModel;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@ToString
public class UserPlanSubscriptionEventOutboxMessage extends BaseMessagingModel {
    private Message payload;
}
