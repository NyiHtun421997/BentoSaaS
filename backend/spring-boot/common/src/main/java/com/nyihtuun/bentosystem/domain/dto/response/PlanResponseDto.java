package com.nyihtuun.bentosystem.domain.dto.response;

import com.nyihtuun.bentosystem.domain.dto.AbstractPlanDto;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PlanResponseDto extends AbstractPlanDto implements Serializable {
    private UUID planId;
    private String code;
    private PlanStatus status;
    private UUID providerUserId;
    private List<PlanMealResponseDto> planMealResponseDtos;
    private String image;
}
