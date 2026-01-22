package com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.request;

import com.nyihtuun.bentosystem.domain.dto.AbstractPlanMealDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@SuperBuilder
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class PlanMealRequestDto extends AbstractPlanMealDto implements Serializable {

}
