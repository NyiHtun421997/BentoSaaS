package com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.request;

import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.AbstractPlanDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Getter
@SuperBuilder
public class PlanRequestDto extends AbstractPlanDto implements Serializable {

    @NotNull
    @Size(min = 1)
    private List<@NotNull @Valid PlanMealRequestDto> planMealRequestDtos;
}
