package com.nyihtuun.bentosystem.subscriptionservice.application_service.impl.service;

import com.google.protobuf.Struct;
import com.google.protobuf.Timestamp;
import com.nyihtuun.bentosystem.domain.event.EventCreationFunction;
import com.nyihtuun.bentosystem.domain.valueobject.status.SubscriptionStatus;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.dto.SubscriptionResponseDto;
import lombok.extern.slf4j.Slf4j;
import subscription.events.UserPlanSubscriptionEvent;
import subscription.notification_events.PlanManagementNotificationEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
public class PlanEventHelper {
    public static final EventCreationFunction<SubscriptionResponseDto, List<UUID>, List<UUID>, UserPlanSubscriptionEvent>
            USER_PLAN_SUBSCRIPTION_EVENT_CREATION_FUNCTION = (subscription, appliedPlanMealIds, unappliedPlanMealIds) -> {
        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                                       .setSeconds(now.getEpochSecond())
                                       .setNanos(now.getNano())
                                       .build();

        return UserPlanSubscriptionEvent.newBuilder()
                                        .setPlanId(subscription.getPlanId().toString())
                                        .setCreatedAt(timestamp)
                                        .addAllAppliedPlanMealIds(appliedPlanMealIds.stream()
                                                                                    .map(UUID::toString)
                                                                                    .toList())
                                        .addAllUnAppliedPlanMealIds(unappliedPlanMealIds.stream()
                                                                                        .map(UUID::toString)
                                                                                        .toList())
                                        .setSubscriptionStatus(getPayloadSubscriptionStatus(subscription.getSubscriptionStatus()))
                                        .build();
    };

    public static final EventCreationFunction<SubscriptionResponseDto, String, Struct, PlanManagementNotificationEvent>
            PLAN_NOTIFICATION_EVENT_CREATION_FUNCTION = (subscription, notificationEventType, payload) -> {
        return PlanManagementNotificationEvent.newBuilder()
                                              .setUserId(
                                                      subscription.getUserId().toString())
                                              .setPlanId(
                                                      subscription.getPlanId().toString())
                                              .setNotificationEventType(
                                                      notificationEventType)
                                              .setPayload(
                                                      payload)
                                              .build();
    };

    private static subscription.events.SubscriptionStatus getPayloadSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        return switch (subscriptionStatus) {
            case APPLIED -> subscription.events.SubscriptionStatus.SUBSCRIPTION_APPLIED;
            case SUBSCRIBED -> subscription.events.SubscriptionStatus.SUBSCRIPTION_SUBSCRIBED;
            case CANCELLED -> subscription.events.SubscriptionStatus.SUBSCRIPTION_CANCELLED;
            case SUSPENDED -> subscription.events.SubscriptionStatus.SUBSCRIPTION_SUSPENDED;
        };
    }
}
