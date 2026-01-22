package com.nyihtuun.bentosystem.planmanagementservice.controller;

import com.nyihtuun.bentosystem.domain.dto.response.PlanResponseDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service.PlanManagementQueryService;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.DeliverySchedule;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementDomainException;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.nyihtuun.bentosystem.planmanagementservice.controller.ApiPaths.*;

@Slf4j
@RestController
@RequestMapping(VERSION1 + PLAN)
@Tag(name = "Plan Query", description = "Endpoints for searching and retrieving bento plans.")
public class PlanQueryController {

    private final PlanManagementQueryService planManagementQueryService;

    @Autowired
    public PlanQueryController(PlanManagementQueryService planManagementQueryService) {
        this.planManagementQueryService = planManagementQueryService;
    }

    @GetMapping
    @Operation(summary = "Search plans", description = "Search for active plans with pagination.")
    public ResponseEntity<List<PlanResponseDto>> findAllActivePlans(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching all active plans");
        List<PlanResponseDto> activePlans = planManagementQueryService.getActivePlans(page, size);
        log.info("Active plans: {}", activePlans);
        return ResponseEntity.ok(activePlans);
    }

    @GetMapping("bytitleandcode")
    @Operation(summary = "Find plan by title and code", description = "Retrieves a specific plan using its exact title and unique code.")
    @ApiResponse(responseCode = "200", description = "Plan found")
    @ApiResponse(responseCode = "400", description = "Plan not found or invalid parameters")
    public ResponseEntity<PlanResponseDto> findPlanByTitleAndCode(
            @Parameter(description = "Plan title") @RequestParam String title,
            @Parameter(description = "Unique plan code") @RequestParam String code) {
        log.info("Fetching active plan by title: {} and code: {}", title, code);
        return planManagementQueryService.getPlanByTitleAndCode(title, code)
                                         .map(planResponseDto -> {
                                             log.info("Found active plan by title: {} and code: {} : {}",
                                                      title,
                                                      code,
                                                      planResponseDto);
                                             return ResponseEntity.ok(planResponseDto);
                                         })
                                         .orElseThrow(() -> {
                                             log.error(
                                                     "No active plan found for title: {} and code: {}",
                                                     title,
                                                     code);
                                             return new PlanManagementDomainException(PlanManagementErrorCode.INVALID_PARAMS);
                                         });
    }

    @GetMapping("bycategory")
    @Operation(summary = "Find plans by category", description = "Retrieves a paginated list of plans belonging to a specific category.")
    public ResponseEntity<List<PlanResponseDto>> findActivePlansByCategory(
            @Parameter(description = "UUID of the category") @RequestParam UUID categoryId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "5") int size) {
        log.info("Fetching active plans by categoryId: {}", categoryId);
        List<PlanResponseDto> plansByCategoryId = planManagementQueryService.getPlansByCategoryId(categoryId, page, size);
        log.info("Active plans by categoryId: {} : {}", categoryId, plansByCategoryId);
        return ResponseEntity.ok(plansByCategoryId);
    }

    @GetMapping("nearby")
    @Operation(summary = "Find plans near location", description = "Retrieves plans within a certain radius of the provided geographic coordinates.")
    public ResponseEntity<List<PlanResponseDto>> findActivePlansNearMe(
            @Parameter(description = "Latitude of the user") @RequestParam double latitude,
            @Parameter(description = "Longitude of the user") @RequestParam double longitude,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "5") int size) {
        log.info("Fetching active plans near me: [{} , {}]", latitude, longitude);
        List<PlanResponseDto> plansNearMe = planManagementQueryService.getPlansNearMe(latitude, longitude, page, size);
        log.info("Active plans near me: [{} , {}] , {}", latitude, longitude, plansNearMe);
        return ResponseEntity.ok(plansNearMe);
    }

    @GetMapping("byuserid")
    @Operation(summary = "Find plans by provider", description = "Retrieves all plans managed by a specific provider user.")
    public ResponseEntity<List<PlanResponseDto>> findMyPlans(
            @Parameter(description = "UUID of the provider user") @RequestParam UUID userId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "5") int size) {
        log.info("Fetching active plans by userId");
        List<PlanResponseDto> plansByUserId = planManagementQueryService.getPlansByUserId(userId);
        log.info("Active plans by userId: {}", plansByUserId);
        return ResponseEntity.ok(plansByUserId);
    }

    @GetMapping("/{planId}")
    @Operation(summary = "Get plan details", description = "Fetches comprehensive details for a specific plan by its ID.")
    @ApiResponse(responseCode = "200", description = "Plan details retrieved")
    @ApiResponse(responseCode = "400", description = "Invalid plan ID")
    public ResponseEntity<PlanResponseDto> findPlanDetails(
            @Parameter(description = "UUID of the plan") @PathVariable UUID planId) {
        log.info("Fetching active plan details for planId: {}", planId);
        return planManagementQueryService.getPlanByPlanId(planId)
                                         .map(planResponseDto -> {
                                             log.info("Active plan details for planId: {} : {}", planId, planResponseDto);
                                             return ResponseEntity.ok(planResponseDto);
                                         })
                                         .orElseThrow(() -> {
                                             log.error(
                                                     "No active plan found for planId: {}",
                                                     planId);
                                             return new PlanManagementDomainException(PlanManagementErrorCode.INVALID_PARAMS);
                                         });

    }

    @GetMapping("/delivery-schedule")
    @Operation(summary = "Get delivery schedule", description = "Retrieves the delivery schedule for a plan within a specific date range.")
    public ResponseEntity<DeliverySchedule> findDeliverySchedulesByPlanIdAndDate(
            @Parameter(description = "UUID of the plan") @RequestParam UUID planId,
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam LocalDate start,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam LocalDate end) {
        log.info("Fetching delivery schedule for planId: {} between dates: {} and {}", planId, start, end);
        return planManagementQueryService.getDeliverySchedulesByPlanIdAndDate(planId, start, end)
                                         .map(ResponseEntity::ok)
                                         .orElseThrow(() -> new PlanManagementDomainException(PlanManagementErrorCode.INVALID_PARAMS));
    }
}
