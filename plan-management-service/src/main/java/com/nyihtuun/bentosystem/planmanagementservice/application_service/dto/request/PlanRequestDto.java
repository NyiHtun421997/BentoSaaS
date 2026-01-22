package com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.request;

import com.nyihtuun.bentosystem.domain.dto.AbstractPlanDto;
import com.nyihtuun.bentosystem.planmanagementservice.controller.validation.CreatePlanValidationGroup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class PlanRequestDto extends AbstractPlanDto implements Serializable {

    @NotNull(groups = CreatePlanValidationGroup.class)
    @Size(min = 1, groups = CreatePlanValidationGroup.class)
    private List<@NotNull(groups = CreatePlanValidationGroup.class) @Valid PlanMealRequestDto> planMealRequestDtos;
}
