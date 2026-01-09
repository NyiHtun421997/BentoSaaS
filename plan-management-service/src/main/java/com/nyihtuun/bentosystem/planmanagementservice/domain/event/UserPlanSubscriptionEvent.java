package com.nyihtuun.bentosystem.planmanagementservice.domain.event;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;

public class UserPlanSubscriptionEvent extends AbstractPlanEvent {

    @Getter
    private final List<PlanMealId> planMealIds;

    public UserPlanSubscriptionEvent(PlanId planId, ZonedDateTime createdAt, List<PlanMealId> planMealIds) {
        super(planId, createdAt);
        this.planMealIds = planMealIds;
    }
}
