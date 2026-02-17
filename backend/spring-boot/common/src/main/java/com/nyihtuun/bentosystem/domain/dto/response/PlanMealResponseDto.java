package com.nyihtuun.bentosystem.domain.dto.response;

import com.nyihtuun.bentosystem.domain.dto.AbstractPlanMealDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.UUID;

@Getter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PlanMealResponseDto extends AbstractPlanMealDto implements Serializable {
    private UUID planMealId;
    private UUID planId;
    private int currentSubCount;
    private String image;
}
