package com.nyihtuun.bentosystem.planmanagementservice.domain.event;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.domain.valueobject.status.SubscriptionStatus;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;

public class UserPlanSubscriptionEvent extends AbstractPlanEvent {

    @Getter
    private final SubscriptionStatus subscriptionStatus;

    public UserPlanSubscriptionEvent(PlanId planId, ZonedDateTime createdAt, List<PlanMealId> planMealIds,
                                     SubscriptionStatus subscriptionStatus) {
        super(planId, createdAt, planMealIds);
        this.subscriptionStatus = subscriptionStatus;
    }
}
