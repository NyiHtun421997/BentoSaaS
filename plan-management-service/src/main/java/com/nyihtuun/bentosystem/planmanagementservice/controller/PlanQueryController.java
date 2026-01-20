package com.nyihtuun.bentosystem.planmanagementservice.controller;

import com.nyihtuun.bentosystem.planmanagementservice.application_service.dto.response.PlanResponseDto;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service.PlanManagementQueryService;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.DeliverySchedule;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementDomainException;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.nyihtuun.bentosystem.planmanagementservice.controller.ApiPaths.PLAN;
import static com.nyihtuun.bentosystem.planmanagementservice.controller.ApiPaths.VERSION1;

@Slf4j
@RestController
@RequestMapping(VERSION1 + PLAN)
public class PlanQueryController {

    private final PlanManagementQueryService planManagementQueryService;

    @Autowired
    public PlanQueryController(PlanManagementQueryService planManagementQueryService) {
        this.planManagementQueryService = planManagementQueryService;
    }

    @GetMapping
    public ResponseEntity<List<PlanResponseDto>> findAllActivePlans(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "5") int size) {
        log.info("Fetching all active plans");
        List<PlanResponseDto> activePlans = planManagementQueryService.getActivePlans(page, size);
        log.info("Active plans: {}", activePlans);
        return ResponseEntity.ok(activePlans);
    }

    @GetMapping("bytitleandcode")
    public ResponseEntity<PlanResponseDto> findPlanByTitleAndCode(@RequestParam String title, @RequestParam String code) {
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
    public ResponseEntity<List<PlanResponseDto>> findActivePlansByCategory(@RequestParam UUID categoryId,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "5") int size) {
        log.info("Fetching active plans by categoryId: {}", categoryId);
        List<PlanResponseDto> plansByCategoryId = planManagementQueryService.getPlansByCategoryId(categoryId, page, size);
        log.info("Active plans by categoryId: {} : {}", categoryId, plansByCategoryId);
        return ResponseEntity.ok(plansByCategoryId);
    }

    @GetMapping("nearby")
    public ResponseEntity<List<PlanResponseDto>> findActivePlansNearMe(@RequestParam double latitude,
                                                                       @RequestParam double longitude,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "5") int size) {
        log.info("Fetching active plans near me: [{} , {}]", latitude, longitude);
        List<PlanResponseDto> plansNearMe = planManagementQueryService.getPlansNearMe(latitude, longitude, page, size);
        log.info("Active plans near me: [{} , {}] , {}", latitude, longitude, plansNearMe);
        return ResponseEntity.ok(plansNearMe);
    }

    @GetMapping("byuserid")
    public ResponseEntity<List<PlanResponseDto>> findMyPlans(@RequestParam UUID userId,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "5") int size) {
        log.info("Fetching active plans by userId");
        List<PlanResponseDto> plansByUserId = planManagementQueryService.getPlansByUserId(userId);
        log.info("Active plans by userId: {}", plansByUserId);
        return ResponseEntity.ok(plansByUserId);
    }

    @GetMapping("/{planId}")
    public ResponseEntity<PlanResponseDto> findPlanDetails(@PathVariable UUID planId) {
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
    public ResponseEntity<DeliverySchedule> getDeliverySchedulesByPlanIdAndDate(@RequestParam UUID planId, @RequestParam
    LocalDate start, @RequestParam LocalDate end) {
        log.info("Fetching delivery schedule for planId: {} between dates: {} and {}", planId, start, end);
        return planManagementQueryService.getDeliverySchedulesByPlanIdAndDate(planId, start, end)
                                         .map(ResponseEntity::ok)
                                         .orElseThrow(() -> new PlanManagementDomainException(PlanManagementErrorCode.INVALID_PARAMS));
    }
}
