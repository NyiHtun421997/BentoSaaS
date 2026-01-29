package com.nyihtuun.bentosystem.planmanagementservice.application_service.impl.input_message;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.message.UserPlanSubscriptionMessageListener;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service.PlanManagementCommandService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Component
public class UserPlanSubscriptionMessageListenerImpl implements UserPlanSubscriptionMessageListener {

    private final PlanManagementCommandService planManagementCommandService;

    @Override
    public void reflectUserPlanSubscription(UUID planId, List<UUID> appliedPlanMealIds, List<UUID> unappliedPlanMealIds) {
        planManagementCommandService.reflectUserSubscription(new PlanId(planId),
                                                             appliedPlanMealIds.stream()
                                                                               .map(PlanMealId::new)
                                                                               .toList(),
                                                             unappliedPlanMealIds.stream()
                                                                                 .map(PlanMealId::new)
                                                                                 .toList());
    }
}
