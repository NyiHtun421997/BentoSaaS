package com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.message;

import com.nyihtuun.bentosystem.subscriptionservice.application_service.outbox.model.UserPlanSubscriptionEventOutboxMessage;
import org.springframework.kafka.support.SendResult;

import java.util.function.BiConsumer;

public interface UserPlanSubscriptionMessagePublisher {
    void publish(UserPlanSubscriptionEventOutboxMessage userPlanSubscriptionEventOutboxMessage,
                 BiConsumer<? super SendResult<String, byte[]>, ? super Throwable> callback);
}
