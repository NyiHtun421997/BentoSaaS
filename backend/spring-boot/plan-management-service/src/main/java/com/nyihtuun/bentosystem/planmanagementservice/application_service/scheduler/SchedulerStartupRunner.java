package com.nyihtuun.bentosystem.planmanagementservice.application_service.scheduler;

import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service.PlanManagementCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"local"})
public class SchedulerStartupRunner {

    private final PlanManagementCommandService commandService;

    public SchedulerStartupRunner(PlanManagementCommandService commandService) {
        this.commandService = commandService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runOnceOnStartup() {
        log.info("Scheduling generation of delivery schedules.");
        commandService.generateSchedules();
        log.info("Delivery schedule generation finished.");
    }
}
