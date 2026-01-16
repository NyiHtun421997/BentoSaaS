package com.nyihtuun.bentosystem.planmanagementservice.controller;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.domain.valueobject.UserId;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.request.PlanMealRequestDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.request.PlanRequestDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.response.PlanResponseDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service.PlanManagementCommandService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.nyihtuun.bentosystem.planmanagementservice.controller.ApiPaths.*;

@Slf4j
@RestController
@RequestMapping(VERSION1 + PROVIDER_PLAN)
public class PlanPlanMealCommandController {

    private final PlanManagementCommandService planManagementCommandService;

    @Autowired
    public PlanPlanMealCommandController(PlanManagementCommandService planManagementCommandService) {
        this.planManagementCommandService = planManagementCommandService;
    }

    @PostMapping
    public ResponseEntity<PlanResponseDto> createPlan(@Valid @RequestBody PlanRequestDto planRequestDto) {
        // TODO : implement authentication and jwt related services
        // temporarily assume we will get user id from jwt
        UUID userId = UUID.randomUUID();
        log.info("Creating plan: {}", planRequestDto);
        PlanResponseDto planResponseDto = planManagementCommandService.validateAndInitiatePlan(planRequestDto, new UserId(userId));
        log.info("Plan created: {}", planResponseDto);
        return ResponseEntity.ok(planResponseDto);
    }

    @PutMapping
    public ResponseEntity<PlanResponseDto> updatePlan(UUID planId,
                                                      @Valid @RequestBody PlanRequestDto planRequestDto) {
        log.info("Updating plan with planId: {} : {}", planId, planRequestDto);
        PlanResponseDto planResponseDto = planManagementCommandService.validateAndUpdatePlanInfo(new PlanId(planId), planRequestDto);
        log.info("Plan with planId: {} updated: {}", planId, planResponseDto);
        return ResponseEntity.ok(planResponseDto);
    }

    @DeleteMapping
    public ResponseEntity<?> deletePlan(UUID planId) {
        log.info("Deleting plan with planId: {}", planId);
        planManagementCommandService.deletePlan(new PlanId(planId));
        log.info("Plan with planId: {} deleted", planId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(MEAL)
    public ResponseEntity<PlanResponseDto> addMeal(UUID planId,
                                                   @Valid @RequestBody PlanMealRequestDto planMealRequestDto) {
        log.info("Adding meal with planId: {} : {}", planId, planMealRequestDto);
        PlanResponseDto planResponseDto = planManagementCommandService.addMealToPlan(new PlanId(planId), planMealRequestDto);
        log.info("PlanMeal added to Plan with planId: {} : {}", planId, planResponseDto);
        return ResponseEntity.ok(planResponseDto);
    }

    @PutMapping(MEAL)
    public ResponseEntity<PlanResponseDto> updateMeal(UUID planId,
                                                      UUID mealId,
                                                      @Valid @RequestBody PlanMealRequestDto planMealRequestDto) {
        log.info("Updating meal with planId: {} and mealId: {} : {}", planId, mealId, planMealRequestDto);
        PlanResponseDto planResponseDto = planManagementCommandService.updateMealFromPlan(new PlanId(planId), new PlanMealId(mealId), planMealRequestDto);
        log.info("PlanMeal with id: {} updated from Plan with planId: {}: {}", mealId, planId, planResponseDto);
        return ResponseEntity.ok(planResponseDto);
    }

    @DeleteMapping(MEAL)
    public ResponseEntity<?> deleteMeal(UUID planId, UUID mealId) {
        log.info("Deleting meal with planId: {} and mealId: {}", planId, mealId);
        planManagementCommandService.removeMealFromPlan(new PlanId(planId), new PlanMealId(mealId));
        log.info("PlanMeal with id: {} deleted from Plan with planId: {}", mealId, planId);
        return ResponseEntity.ok().build();
    }
}
