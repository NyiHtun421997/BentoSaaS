package com.nyihtuun.bentosystem.planmanagementservice.application_service.outbox.scheduler;

import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository.PlanChangedEventOutboxRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class PlanChangedEventOutboxCleaner {
    private final PlanChangedEventOutboxRepository planChangedEventOutboxRepository;

    @Scheduled(cron = "@midnight")
    public void cleanUp() {
        log.info("Cleaning up PlanChangedEventOutbox messages");
        planChangedEventOutboxRepository.deleteByOutboxStatus(OutboxStatus.COMPLETED);
        log.info("PlanChangedEventOutbox cleanup completed");
    }
}
