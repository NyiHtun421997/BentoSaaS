package com.nyihtuun.bentosystem.planmanagementservice.domain.event;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor
public abstract class AbstractPlanEvent {
    protected final PlanId planId;
    protected final ZonedDateTime createdAt;
}
