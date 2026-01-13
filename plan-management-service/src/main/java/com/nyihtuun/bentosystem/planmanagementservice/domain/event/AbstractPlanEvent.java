package com.nyihtuun.bentosystem.planmanagementservice.domain.event;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public abstract class AbstractPlanEvent {
    private final PlanId planId;
    private final ZonedDateTime createdAt;
    private final List<PlanMealId> planMealIds;
}
