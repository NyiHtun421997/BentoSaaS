package com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.domain.valueobject.UserId;
import com.nyihtuun.bentosystem.domain.valueobject.status.SubscriptionStatus;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.request.PlanRequestDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.request.PlanMealRequestDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.response.PlanResponseDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.mapper.PlanDataMapper;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository.PlanManagementRepository;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.DeliverySchedule;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Plan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class PlanManagementCommandServiceImpl implements PlanManagementCommandService {

    private final PlanDataMapper planDataMapper;
    private final PlanManagementRepository planManagementRepository;

    @Autowired
    public PlanManagementCommandServiceImpl(PlanDataMapper planDataMapper, PlanManagementRepository planManagementRepository) {
        this.planDataMapper = planDataMapper;
        this.planManagementRepository = planManagementRepository;
    }

    @Override
    @Transactional
    public PlanResponseDto validateAndInitiatePlan(PlanRequestDto planRequestDto, UserId userId) {
        Plan plan = planDataMapper.mapPlanDtoToPlan(planRequestDto, userId);
        plan.validatePlan();
        plan.initializePlan();

        log.info("Plan with id: {} is initiated", plan.getId().getValue());

        Plan savedPlan = planManagementRepository.save(plan);
        log.info("Plan with id: {} is persisted", plan.getId().getValue());
        return planDataMapper.mapPlanToPlanDto(savedPlan);
    }

    @Override
    @Transactional
    public PlanResponseDto validateAndUpdatePlanInfo(PlanId planId, PlanRequestDto planRequestDto) {
        return null;
    }

    @Override
    @Transactional
    public void deletePlan(PlanId planId) {

    }

    @Override
    @Transactional
    public PlanResponseDto reflectUserSubscription(PlanId planId, List<PlanMealId> planMealIds, SubscriptionStatus subscriptionStatus) {
        return null;
    }

    @Override
    @Transactional
    public PlanResponseDto addMealToPlan(PlanId planId, PlanMealRequestDto planMealRequestDto) {
        return null;
    }

    @Override
    @Transactional
    public PlanResponseDto removeMealFromPlan(PlanId planId, PlanMealId planMealId) {
        return null;
    }

    @Override
    @Transactional
    public PlanResponseDto updateMealFromPlan(PlanId planId, PlanMealId planMealId, PlanMealRequestDto planMealRequestDto) {
        return null;
    }

    @Override
    @Transactional
    public List<DeliverySchedule> generateSchedules() {
        return List.of();
    }

    @Override
    @Transactional
    public void createCategory(String category) {

    }
}
