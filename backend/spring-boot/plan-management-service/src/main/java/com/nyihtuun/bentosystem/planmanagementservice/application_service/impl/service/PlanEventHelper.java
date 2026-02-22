package com.nyihtuun.bentosystem.planmanagementservice.application_service.impl.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.JsonFormat;
import com.nyihtuun.bentosystem.domain.event.EventCreationFunction;
import lombok.extern.slf4j.Slf4j;
import plan_management.events.PlanChangedEvent;
import plan_management.notification_events.PlanManagementNotificationEvent;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanMealStatus;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Plan;

import java.time.Instant;

@Slf4j
public class PlanEventHelper {
    static final EventCreationFunction<Plan, PlanStatus, PlanMealStatus, PlanChangedEvent>
            PLAN_CHANGED_EVENT_CREATION_FUNCTION = (plan, planStatus, planMealStatus) -> {
        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                                       .setSeconds(now.getEpochSecond())
                                       .setNanos(now.getNano())
                                       .build();

        return PlanChangedEvent.newBuilder()
                               .setPlanId(plan.getId().getValue().toString())
                               .setCreatedAt(timestamp)
                               .addAllPlanMealIds(plan.getPlanMeals()
                                                      .stream()
                                                      .map(planMeal -> planMeal.getId()
                                                                               .getValue()
                                                                               .toString())
                                                      .toList())
                               .setPlanStatus(plan_management.events.PlanStatus.valueOf(planStatus.toString()))
                               .setPlanMealStatus(plan_management.events.PlanMealStatus.valueOf(
                                       planMealStatus.toString()))
                               .build();
    };

    static final EventCreationFunction<Plan, String, ObjectNode, PlanManagementNotificationEvent>
            PLAN_NOTIFICATION_EVENT_CREATION_FUNCTION = (plan, notificationEventType, payload) -> {
        return PlanManagementNotificationEvent.newBuilder()
                                              .setUserId(plan.getProviderUserId().getValue().toString())
                                              .setPlanId(plan.getId().getValue().toString())
                                              .setNotificationEventType(notificationEventType)
                                              .setPayload(constructPayload(payload))
                                              .build();
    };

    private static Struct constructPayload(ObjectNode payload) {
        Struct.Builder builder = Struct.newBuilder();
        try {
            JsonFormat.parser().merge(payload.toString(), builder);
        } catch (InvalidProtocolBufferException e) {
            log.error("Could not construct payload from JSON!", e);
            throw new RuntimeException(e);
        }
        return builder.build();
    }
}
