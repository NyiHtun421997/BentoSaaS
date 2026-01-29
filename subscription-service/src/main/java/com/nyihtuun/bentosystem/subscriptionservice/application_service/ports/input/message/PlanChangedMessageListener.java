package com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.input.message;

import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;

import java.util.List;
import java.util.UUID;

public interface PlanChangedMessageListener {
    void reflectPlanChanged(UUID planId, PlanStatus planStatus);
    void reflectPlanMealsRemoved(UUID planId, List<UUID> planMealIds);
}
