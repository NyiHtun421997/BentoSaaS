package com.nyihtuun.bentosystem.planmanagementservice.controller;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import com.nyihtuun.bentosystem.domain.valueobject.UserId;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.request.PlanMealRequestDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.request.PlanRequestDto;
import com.nyihtuun.bentosystem.domain.dto.response.PlanResponseDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service.PlanManagementCommandService;
import com.nyihtuun.bentosystem.planmanagementservice.controller.validation.CreatePlanValidationGroup;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.UUID;

import static com.nyihtuun.bentosystem.planmanagementservice.controller.ApiPaths.*;

@Slf4j
@RestController
@RequestMapping(VERSION1 + PROVIDER_PLAN)
@Tag(name = "Plan Command", description = "Endpoints for creating, updating, and deleting plans and their associated meals.")
public class PlanPlanMealCommandController {

    private final PlanManagementCommandService planManagementCommandService;

    @Autowired
    public PlanPlanMealCommandController(PlanManagementCommandService planManagementCommandService) {
        this.planManagementCommandService = planManagementCommandService;
    }

    @PostMapping
    @Operation(summary = "Create a new plan", description = "Initiates a new bento plan for a provider.")
    @ApiResponse(responseCode = "200", description = "Plan created successfully")
    public ResponseEntity<PlanResponseDto> createPlan(@Validated({Default.class, CreatePlanValidationGroup.class}) @RequestBody PlanRequestDto planRequestDto) {
        // TODO : implement authentication and jwt related services
        // temporarily assume we will get user id from jwt
        UUID userId = UUID.randomUUID();
        log.info("Creating plan: {}", planRequestDto);
        PlanResponseDto planResponseDto = planManagementCommandService.validateAndInitiatePlan(planRequestDto, new UserId(userId));
        log.info("Plan created: {}", planResponseDto);
        return ResponseEntity.ok(planResponseDto);
    }

    @PutMapping(PLAN_ID)
    @Operation(summary = "Update plan info", description = "Updates basic information of an existing plan.")
    @ApiResponse(responseCode = "200", description = "Plan updated successfully")
    public ResponseEntity<PlanResponseDto> updatePlan(@PathVariable UUID planId,
                                                      @Validated({Default.class}) @RequestBody PlanRequestDto planRequestDto) {
        log.info("Updating plan with planId: {} : {}", planId, planRequestDto);
        PlanResponseDto planResponseDto = planManagementCommandService.validateAndUpdatePlanInfo(new PlanId(planId), planRequestDto);
        log.info("Plan with planId: {} updated: {}", planId, planResponseDto);
        return ResponseEntity.ok(planResponseDto);
    }

    @DeleteMapping(PLAN_ID)
    @Operation(summary = "Delete a plan", description = "Soft deletes a plan by ID.")
    @ApiResponse(responseCode = "200", description = "Plan deleted successfully")
    public ResponseEntity<?> deletePlan(@PathVariable UUID planId) {
        log.info("Deleting plan with planId: {}", planId);
        planManagementCommandService.deletePlan(new PlanId(planId));
        log.info("Plan with planId: {} deleted", planId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(MEAL + PLAN_ID)
    @Operation(summary = "Add meal to plan", description = "Adds a new meal option to an existing plan.")
    @ApiResponse(responseCode = "200", description = "Meal added successfully")
    public ResponseEntity<PlanResponseDto> addMeal(@PathVariable UUID planId,
                                                   @Valid @RequestBody PlanMealRequestDto planMealRequestDto) {
        log.info("Adding meal with planId: {} : {}", planId, planMealRequestDto);
        PlanResponseDto planResponseDto = planManagementCommandService.addMealToPlan(new PlanId(planId), planMealRequestDto);
        log.info("PlanMeal added to Plan with planId: {} : {}", planId, planResponseDto);
        return ResponseEntity.ok(planResponseDto);
    }

    @PutMapping(MEAL)
    @Operation(summary = "Update a meal", description = "Updates details of a specific meal within a plan.")
    @ApiResponse(responseCode = "200", description = "Meal updated successfully")
    public ResponseEntity<PlanResponseDto> updateMeal(@RequestParam UUID planId,
                                                      @RequestParam UUID mealId,
                                                      @Valid @RequestBody PlanMealRequestDto planMealRequestDto) {
        log.info("Updating meal with planId: {} and mealId: {} : {}", planId, mealId, planMealRequestDto);
        PlanResponseDto planResponseDto = planManagementCommandService.updateMealFromPlan(new PlanId(planId), new PlanMealId(mealId), planMealRequestDto);
        log.info("PlanMeal with id: {} updated from Plan with planId: {}: {}", mealId, planId, planResponseDto);
        return ResponseEntity.ok(planResponseDto);
    }

    @DeleteMapping(MEAL)
    @Operation(summary = "Remove a meal", description = "Removes a specific meal from a plan.")
    @ApiResponse(responseCode = "200", description = "Meal removed successfully")
    public ResponseEntity<?> deleteMeal(@RequestParam UUID planId, @RequestParam UUID mealId) {
        log.info("Deleting meal with planId: {} and mealId: {}", planId, mealId);
        planManagementCommandService.removeMealFromPlan(new PlanId(planId), new PlanMealId(mealId));
        log.info("PlanMeal with id: {} deleted from Plan with planId: {}", mealId, planId);
        return ResponseEntity.ok().build();
    }
}
