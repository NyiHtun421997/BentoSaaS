package com.nyihtuun.bentosystem.subscriptionservice.application_service.outbox.scheduler;

import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.repository.UserPlanSubscriptionEventOutboxRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class UserPlanSubscriptionEventOutboxCleaner {
    private final UserPlanSubscriptionEventOutboxRepository userPlanSubscriptionEventOutboxRepository;

    @Scheduled(cron = "@midnight")
    public void cleanUp() {
        log.info("Cleaning up UserPlanSubscriptionEventOutbox messages");
        userPlanSubscriptionEventOutboxRepository.deleteByOutboxStatus(OutboxStatus.COMPLETED);
        log.info("UserPlanSubscriptionEventOutbox cleanup completed");
    }
}
