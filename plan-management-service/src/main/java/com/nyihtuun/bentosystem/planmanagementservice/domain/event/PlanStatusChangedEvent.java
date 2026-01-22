package com.nyihtuun.bentosystem.planmanagementservice.domain.event;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanMealStatus;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class PlanStatusChangedEvent extends AbstractPlanEvent {

    private final List<PlanMealId> planMealIds;
    private final PlanStatus planStatus;
    private final PlanMealStatus planMealStatus;

    public PlanStatusChangedEvent(PlanId planId, Instant createdAt, List<PlanMealId> planMealIds, PlanStatus planStatus,
                                  PlanMealStatus planMealStatus) {
        super(planId, createdAt);
        this.planMealIds = planMealIds;
        this.planStatus = planStatus;
        this.planMealStatus = planMealStatus;
    }
}
