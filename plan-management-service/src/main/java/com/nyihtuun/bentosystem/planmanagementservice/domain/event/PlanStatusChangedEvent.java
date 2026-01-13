package com.nyihtuun.bentosystem.planmanagementservice.domain.event;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;

public class PlanStatusChangedEvent extends AbstractPlanEvent {

    @Getter
    private final PlanStatus newStatus;

    public PlanStatusChangedEvent(PlanId planId, ZonedDateTime createdAt, List<PlanMealId> planMealIds, PlanStatus newStatus) {
        super(planId, createdAt, planMealIds);
        this.newStatus = newStatus;
    }
}
