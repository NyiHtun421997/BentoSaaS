package com.nyihtuun.bentosystem.planmanagementservice.domain.event;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;

import java.time.ZonedDateTime;

public class PlanStatusChangedEvent extends AbstractPlanEvent {
    public PlanStatusChangedEvent(PlanId planId, ZonedDateTime createdAt) {
        super(planId, createdAt);
    }
}
