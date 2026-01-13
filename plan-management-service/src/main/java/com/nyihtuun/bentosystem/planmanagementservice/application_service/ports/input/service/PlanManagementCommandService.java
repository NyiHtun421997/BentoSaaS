package com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.domain.valueobject.UserId;
import com.nyihtuun.bentosystem.domain.valueobject.status.SubscriptionStatus;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.request.PlanRequestDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.request.PlanMealRequestDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.response.PlanResponseDto;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.DeliverySchedule;

import java.util.List;

public interface PlanManagementCommandService {

    PlanResponseDto validateAndInitiatePlan(PlanRequestDto planRequestDto, UserId userId);

    PlanResponseDto validateAndUpdatePlanInfo(PlanId planId, PlanRequestDto planRequestDto);

    void deletePlan(PlanId planId);

    PlanResponseDto reflectUserSubscription(PlanId planId, List<PlanMealId> planMealIds, SubscriptionStatus subscriptionStatus);

    PlanResponseDto addMealToPlan(PlanId planId, PlanMealRequestDto planMealRequestDto);

    PlanResponseDto removeMealFromPlan(PlanId planId, PlanMealId planMealId);

    PlanResponseDto updateMealFromPlan(
            PlanId planId,
            PlanMealId planMealId,
            PlanMealRequestDto planMealRequestDto
    );

    List<DeliverySchedule> generateSchedules();

    void createCategory(String category);
}