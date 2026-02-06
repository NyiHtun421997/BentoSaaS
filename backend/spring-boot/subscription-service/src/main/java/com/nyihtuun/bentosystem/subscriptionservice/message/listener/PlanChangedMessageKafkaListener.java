package com.nyihtuun.bentosystem.subscriptionservice.message.listener;

import com.google.protobuf.InvalidProtocolBufferException;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.message.PlanChangedMessageListener;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import subscription.events.PlanChangedEvent;
import subscription.events.PlanMealStatus;

import java.util.UUID;

import static com.nyihtuun.bentosystem.subscriptionservice.SubscriptionConstants.SUBSCRIPTION_GROUP_ID;
import static com.nyihtuun.bentosystem.subscriptionservice.SubscriptionConstants.SUBSCRIPTION_PLAN_CHANGED_TOPIC_NAME;

@Slf4j
@AllArgsConstructor
@Component
public class PlanChangedMessageKafkaListener {

    private final PlanChangedMessageListener planChangedMessageListener;

    @KafkaListener(topics = SUBSCRIPTION_PLAN_CHANGED_TOPIC_NAME, id = SUBSCRIPTION_GROUP_ID)
    public void consumeEvent(byte[] event) {
        try {
            PlanChangedEvent planChangedEvent = PlanChangedEvent.parseFrom(event);
            log.info("Received PlanChangedEvent: {}", planChangedEvent);

            if (planChangedEvent.getPlanMealStatus() == PlanMealStatus.UNCHANGED) {
                log.info("Processing Plan Status {} for plan with id: {}",
                         planChangedEvent.getPlanStatus(),
                         planChangedEvent.getPlanId());
                planChangedMessageListener.reflectPlanChanged(UUID.fromString(planChangedEvent.getPlanId()),
                                                              PlanStatus.valueOf(planChangedEvent.getPlanStatus().toString()));
            }

            if (planChangedEvent.getPlanMealStatus() == PlanMealStatus.MEALS_REMOVED) {
                log.info("Processing Plan Meals Removed for plan with id: {}", planChangedEvent.getPlanId());
                planChangedMessageListener.reflectPlanMealsRemoved(UUID.fromString(planChangedEvent.getPlanId()),
                                                                   planChangedEvent.getPlanMealIdsList().stream()
                                                                                   .map(UUID::fromString)
                                                                                   .toList());
            }
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse PlanChangedEvent from Kafka message", e);
        }
    }
}
