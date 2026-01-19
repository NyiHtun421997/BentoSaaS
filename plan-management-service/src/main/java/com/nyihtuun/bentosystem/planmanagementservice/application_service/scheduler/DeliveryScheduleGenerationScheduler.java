package com.nyihtuun.bentosystem.planmanagementservice.application_service.scheduler;

import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service.PlanManagementCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeliveryScheduleGenerationScheduler {

    private final PlanManagementCommandService planManagementCommandService;

    @Autowired
    public DeliveryScheduleGenerationScheduler(PlanManagementCommandService planManagementCommandService) {
        this.planManagementCommandService = planManagementCommandService;
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    public void process() {
        log.info("Delivery schedule generation task started.");
        planManagementCommandService.generateSchedules();
        log.info("Delivery schedule generation task finished.");
    }
}
