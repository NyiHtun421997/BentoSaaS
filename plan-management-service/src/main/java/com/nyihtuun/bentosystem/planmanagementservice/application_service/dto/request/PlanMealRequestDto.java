package com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.request;

import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.AbstractPlanMealDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@SuperBuilder
public class PlanMealRequestDto extends AbstractPlanMealDto implements Serializable {

}
