package com.nyihtuun.bentosystem.subscriptionservice.message.listener;

import com.google.protobuf.InvalidProtocolBufferException;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.message.PlanChangedMessageListener;
import com.nyihtuun.bentosystem.subscriptionservice.configuration.SubscriptionConfigData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import subscription.events.PlanChangedEvent;
import subscription.events.PlanMealStatus;
import subscription.notification_events.PlanManagementNotificationEvent;

import java.util.UUID;

import static com.nyihtuun.bentosystem.subscriptionservice.SubscriptionConstants.SUBSCRIPTION_GROUP_ID;
import static com.nyihtuun.bentosystem.subscriptionservice.SubscriptionConstants.SUBSCRIPTION_LISTEN_TOPIC_NAMES;

@Slf4j
@AllArgsConstructor
@Component
public class PlanChangedMessageKafkaListener {

    private final PlanChangedMessageListener planChangedMessageListener;
    private final SubscriptionConfigData subscriptionConfigData;

    @KafkaListener(topics = SUBSCRIPTION_LISTEN_TOPIC_NAMES, id = SUBSCRIPTION_GROUP_ID)
    public void consumeEvent(byte[] event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            log.info("Received PlanChangedEvent");

            if (topic.equals(subscriptionConfigData.planChangedTopicName())) {
                PlanChangedEvent planChangedEvent = PlanChangedEvent.parseFrom(event);

                if (planChangedEvent.getPlanMealStatus() == PlanMealStatus.MEALS_REMOVED) {
                    log.info("Processing Plan Meals Removed for plan with id: {}", planChangedEvent.getPlanId());
                    planChangedMessageListener.reflectPlanMealsRemoved(UUID.fromString(planChangedEvent.getPlanId()),
                                                                       planChangedEvent.getPlanMealIdsList().stream()
                                                                                       .map(UUID::fromString)
                                                                                       .toList());
                } else {
                    log.info("Processing Plan Status {} for plan with id: {}",
                             planChangedEvent.getPlanStatus(),
                             planChangedEvent.getPlanId());
                    planChangedMessageListener.reflectPlanChanged(UUID.fromString(planChangedEvent.getPlanId()),
                                                                  PlanStatus.valueOf(planChangedEvent.getPlanStatus().toString()));
                }
            } else if (topic.equals(subscriptionConfigData.planChangedNotificationTopicName())) {
                PlanManagementNotificationEvent planManagementNotificationEvent = PlanManagementNotificationEvent.parseFrom(event);
                planChangedMessageListener.respondPlanChangedNotificationEvent(planManagementNotificationEvent);

                log.info("Processing Plan Changed Notification for plan with id: {}", planManagementNotificationEvent.getPlanId());
            }
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse PlanChangedEvent from Kafka message", e);
        }
    }
}
