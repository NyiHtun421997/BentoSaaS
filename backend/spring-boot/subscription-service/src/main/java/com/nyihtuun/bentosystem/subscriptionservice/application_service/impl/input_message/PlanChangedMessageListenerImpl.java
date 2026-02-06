package com.nyihtuun.bentosystem.subscriptionservice.application_service.impl.input_message;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.message.PlanChangedMessageListener;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.service.SubscriptionCommandService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Component
public class PlanChangedMessageListenerImpl implements PlanChangedMessageListener {

    private final SubscriptionCommandService subscriptionCommandService;

    @Override
    public void reflectPlanChanged(UUID planId, PlanStatus planStatus) {
        subscriptionCommandService.reflectPlanChanged(new PlanId(planId), planStatus);
    }

    @Override
    public void reflectPlanMealsRemoved(UUID planId, List<UUID> planMealIds) {
        subscriptionCommandService.reflectPlanMealsRemoved(new PlanId(planId), planMealIds.stream().map(PlanMealId::new).toList());
    }
}
