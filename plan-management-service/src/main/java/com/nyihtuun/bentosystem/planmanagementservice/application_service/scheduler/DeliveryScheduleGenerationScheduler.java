package com.nyihtuun.bentosystem.planmanagementservice.application_service.scheduler;

import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service.PlanManagementCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.nyihtuun.bentosystem.planmanagementservice.PlanManagementConstants.PLAN_MANAGEMENT_SCHEDULER_CRON;
import static com.nyihtuun.bentosystem.planmanagementservice.PlanManagementConstants.PLAN_MANAGEMENT_ZONE;

@Slf4j
@Component
public class DeliveryScheduleGenerationScheduler {

    private final PlanManagementCommandService planManagementCommandService;

    @Autowired
    public DeliveryScheduleGenerationScheduler(PlanManagementCommandService planManagementCommandService) {
        this.planManagementCommandService = planManagementCommandService;
    }

    @Scheduled(cron = PLAN_MANAGEMENT_SCHEDULER_CRON, zone = PLAN_MANAGEMENT_ZONE)
    public void process() {
        log.info("Delivery schedule generation task started.");
        planManagementCommandService.generateSchedules();
        log.info("Delivery schedule generation task finished.");
    }
}
