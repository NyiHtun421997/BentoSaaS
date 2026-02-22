package com.nyihtuun.bentosystem.invoiceservice.application_service.outbox.scheduler;

import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import com.nyihtuun.bentosystem.invoiceservice.application_service.ports.output.repository.InvoiceEventOutboxRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@AllArgsConstructor
@Component
public class InvoiceEventOutboxCleaner {

    private final InvoiceEventOutboxRepository invoiceEventOutboxRepository;

    @Scheduled(cron = "@midnight")
    @Transactional
    public void cleanUp() {
        log.info("Cleaning up InvoiceEventOutbox messages");
        invoiceEventOutboxRepository.deleteByOutboxStatus(OutboxStatus.COMPLETED);
        log.info("InvoiceEventOutbox cleanup completed");
    }
}
