package com.nyihtuun.bentosystem.planmanagementservice.domain.event;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.domain.valueobject.status.SubscriptionStatus;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class UserPlanSubscriptionEvent extends AbstractPlanEvent {

    private final List<PlanMealId> appliedPlanMealIds;
    private final List<PlanMealId> unappliedPlanMealIds;
    private final SubscriptionStatus subscriptionStatus;

    public UserPlanSubscriptionEvent(PlanId planId, Instant createdAt,
                                     List<PlanMealId> appliedPlanMealIds,
                                     List<PlanMealId> unappliedPlanMealIds,
                                     SubscriptionStatus subscriptionStatus) {
        super(planId, createdAt);
        this.appliedPlanMealIds = appliedPlanMealIds;
        this.unappliedPlanMealIds = unappliedPlanMealIds;
        this.subscriptionStatus = subscriptionStatus;
    }
}
