package com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.message;

import com.nyihtuun.bentosystem.planmanagementservice.application_service.outbox.model.PlanOutboxMessage;
import org.springframework.kafka.support.SendResult;

import java.util.function.BiConsumer;

public interface PlanChangedMessagePublisher {
    void publish(PlanOutboxMessage planOutboxMessage,
                 BiConsumer<? super SendResult<String, byte[]>, ? super Throwable> callback);
}
