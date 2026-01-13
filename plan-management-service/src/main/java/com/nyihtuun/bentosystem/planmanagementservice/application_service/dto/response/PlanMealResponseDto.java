package com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.response;

import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.AbstractPlanMealDto;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.UUID;

@Getter
@SuperBuilder
public class PlanMealResponseDto extends AbstractPlanMealDto implements Serializable {
    private UUID planMealId;
    private UUID planId;
    private int currentSubCount;
}
