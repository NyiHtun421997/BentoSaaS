package com.nyihtuun.bentosystem.subscriptionservice.application_service.impl.input_message;

import com.google.protobuf.Message;
import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.message.PlanChangedMessageListener;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.service.SubscriptionCommandService;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.service.SubscriptionQueryService;
import com.nyihtuun.bentosystem.subscriptionservice.configuration.SubscriptionConfigData;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import subscription.notification_events.PlanManagementNotificationEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static com.nyihtuun.bentosystem.domain.utility.CommonConstants.NOTIFICATION;
import static com.nyihtuun.bentosystem.subscriptionservice.application_service.impl.service.PlanEventHelper.PLAN_NOTIFICATION_EVENT_CREATION_FUNCTION;


@AllArgsConstructor
@Component
public class PlanChangedMessageListenerImpl implements PlanChangedMessageListener {

    private final SubscriptionCommandService subscriptionCommandService;
    private final SubscriptionQueryService subscriptionQueryService;
    private final SubscriptionConfigData subscriptionConfigData;

    @Override
    public void reflectPlanChanged(UUID planId, PlanStatus planStatus) {
        subscriptionCommandService.reflectPlanChanged(new PlanId(planId), planStatus);
    }

    @Override
    public void reflectPlanMealsRemoved(UUID planId, List<UUID> planMealIds) {
        subscriptionCommandService.reflectPlanMealsRemoved(new PlanId(planId), planMealIds.stream().map(PlanMealId::new).toList());
    }

    @Override
    public void respondPlanChangedNotificationEvent(PlanManagementNotificationEvent planManagementNotificationEvent) {
                subscriptionQueryService.getSubscriptionsByPlanId(
                                                UUID.fromString(planManagementNotificationEvent.getPlanId()))
                        .forEach(subscription ->
                                 {
                                     Supplier<Message> eventCreationSupplier =
                                             () -> PLAN_NOTIFICATION_EVENT_CREATION_FUNCTION.execute(subscription,
                                                                                                     planManagementNotificationEvent.getNotificationEventType(),
                                                                                                     planManagementNotificationEvent.getPayload());
                                     subscriptionCommandService.createOutboxMessageAndPersist(subscription,
                                                                                              eventCreationSupplier,
                                                                                              NOTIFICATION,
                                                                                              subscriptionConfigData.userNotificationTopicName());
                                 });
    }
}
