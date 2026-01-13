package com.nyihtuun.bentosystem.planmanagementservice.domain.service;

import com.nyihtuun.bentosystem.domain.valueobject.DeliveryScheduleDetailId;
import com.nyihtuun.bentosystem.domain.valueobject.DeliveryScheduleId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.status.PlanStatus;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.DeliverySchedule;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.DeliveryScheduleDetail;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Plan;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.PlanMeal;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
public class PlanManagementDomainServiceImpl implements PlanManagementDomainService {
    @Override
    public GenerateSchedulesResult generateSchedules(Plan plan, PeriodContext periodContext) {
        if (plan.isDeleteFlag() || !PlanStatus.ACTIVE.equals(plan.getStatus()))
            return generateFailureResult(plan.getId(), PlanManagementErrorCode.INELIGIBLE_PLAN);

        List<LocalDate> skipDays = plan.getSkipDays() == null ? List.of() : plan.getSkipDays();
        List<LocalDate> effectiveBusinessDays = periodContext.businessDaysOfCurrentMonth().stream()
                                                             .filter(businessDay -> !skipDays.contains(businessDay))
                                                             .sorted(Comparator.naturalOrder())
                                                             .toList();

        PeriodContext effectivePeriodContext = new PeriodContext(periodContext.periodStart(),
                                                                 periodContext.periodEnd(),
                                                                 effectiveBusinessDays);
        return generateSuccessfulResult(plan, effectivePeriodContext);
    }

    private GenerateSchedulesResult generateFailureResult(PlanId planId, PlanManagementErrorCode errorCode) {
        return GenerateSchedulesResult.builder()
                                      .planId(planId)
                                      .success(false)
                                      .errorCode(errorCode)
                                      .build();
    }

    private GenerateSchedulesResult generateSuccessfulResult(Plan plan, PeriodContext periodContext) {
        List<LocalDate> businessDays = periodContext.businessDaysOfCurrentMonth();
        // filter only active planmeals
        List<PlanMeal> activePlanMeals = plan.getPlanMeals().stream()
                                             .filter(planMeal -> planMeal.getMinSubCount().isMetBy(planMeal.getCurrentSubCount()))
                                             .sorted(Comparator.comparing(PlanMeal::isPrimary).reversed()
                                                               .thenComparing(PlanMeal::getName))
                                             .toList();

        if (activePlanMeals.isEmpty())
            return generateFailureResult(plan.getId(), PlanManagementErrorCode.EMPTY_MEALS);

        // generate delivery schedules in round and robin
        DeliveryScheduleId deliveryScheduleId = new DeliveryScheduleId(UUID.randomUUID());
        List<DeliveryScheduleDetail> deliveryScheduleDetails = new ArrayList<>();

        for (int i = 0; i < periodContext.businessDaysOfCurrentMonth().size(); i++) {
            PlanMeal meal = activePlanMeals.get(i % activePlanMeals.size());
            DeliveryScheduleDetail deliveryScheduleDetail =
                    DeliveryScheduleDetail.builder()
                                          .deliveryScheduleDetailId(
                                                  new DeliveryScheduleDetailId(
                                                          UUID.randomUUID()))
                                          .deliveryScheduleId(deliveryScheduleId)
                                          .planMealId(meal.getId())
                                          .deliveryDate(businessDays.get(i))
                                          .build();
            deliveryScheduleDetails.add(deliveryScheduleDetail);
        }

        DeliverySchedule deliverySchedule = DeliverySchedule.builder()
                                                 .deliveryScheduleId(deliveryScheduleId)
                                                 .planId(plan.getId())
                                                 .code(plan.getCode())
                                                 .createAt(ZonedDateTime.now())
                                                 .periodStart(periodContext.periodStart())
                                                 .periodEnd(periodContext.periodEnd())
                                                 .deliverySchedules(deliveryScheduleDetails)
                                                 .build();
        return GenerateSchedulesResult.builder()
                                      .planId(plan.getId())
                                      .success(true)
                                      .deliverySchedule(deliverySchedule)
                                      .build();
    }
}
