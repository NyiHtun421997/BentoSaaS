package com.nyihtuun.bentosystem.domain.dto.response;

import com.nyihtuun.bentosystem.domain.dto.AbstractPlanDto;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = true)
public class PlanResponseDto extends AbstractPlanDto implements Serializable {
    private UUID planId;
    private String code;
    private PlanStatus status;
    private List<PlanMealResponseDto> planMealResponseDtos;
}
