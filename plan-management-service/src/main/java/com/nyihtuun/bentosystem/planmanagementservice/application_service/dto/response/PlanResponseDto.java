package com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.response;

import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.AbstractPlanDto;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
@SuperBuilder
public class PlanResponseDto extends AbstractPlanDto implements Serializable {
    private UUID planId;
    private String code;
    private PlanStatus status;
    @NotNull
    private List<PlanMealResponseDto> planMealResponseDtos;
}
