package com.nyihtuun.bentosystem.planmanagementservice.message.listener;

import com.google.protobuf.InvalidProtocolBufferException;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.message.UserPlanSubscriptionMessageListener;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import plan_management.events.UserPlanSubscriptionEvent;

import java.util.List;
import java.util.UUID;

import static com.nyihtuun.bentosystem.planmanagementservice.PlanManagementConstants.PLAN_MANAGEMENT_GROUP_ID;
import static com.nyihtuun.bentosystem.planmanagementservice.PlanManagementConstants.PLAN_MANAGEMENT_USER_SUBSCRIPTION_TOPIC_NAME;

@Slf4j
@AllArgsConstructor
@Component
public class UserPlanSubscriptionMessageKafkaListener {
    private final UserPlanSubscriptionMessageListener userPlanSubscriptionMessageListener;

    @KafkaListener(topics = PLAN_MANAGEMENT_USER_SUBSCRIPTION_TOPIC_NAME, id = PLAN_MANAGEMENT_GROUP_ID)
    public void consumeEvent(byte[] event) {
        try {
            UserPlanSubscriptionEvent userPlanSubscriptionEvent = UserPlanSubscriptionEvent.parseFrom(event);
            log.info("Received UserPlanSubscriptionEvent: {}", userPlanSubscriptionEvent);

            UUID planId = UUID.fromString(userPlanSubscriptionEvent.getPlanId());

            List<UUID> appliedPlanMealIds = userPlanSubscriptionEvent.getAppliedPlanMealIdsList().stream()
                                                                     .map(UUID::fromString)
                                                                     .toList();

            List<UUID> unappliedPlanMealIds = userPlanSubscriptionEvent.getUnAppliedPlanMealIdsList().stream()
                                                                       .map(UUID::fromString)
                                                                       .toList();

            log.info("Processing UserPlanSubscriptionEvent for plan with id: {} and subscription status: {}", planId, userPlanSubscriptionEvent.getSubscriptionStatus());
            userPlanSubscriptionMessageListener.reflectUserPlanSubscription(planId, appliedPlanMealIds, unappliedPlanMealIds);
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse UserPlanSubscriptionEvent from Kafka message", e);
        }
    }
}
