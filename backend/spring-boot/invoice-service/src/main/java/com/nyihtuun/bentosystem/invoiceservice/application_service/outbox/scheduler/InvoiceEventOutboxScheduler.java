package com.nyihtuun.bentosystem.invoiceservice.application_service.outbox.scheduler;

import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import com.nyihtuun.bentosystem.invoiceservice.application_service.outbox.model.InvoiceEventOutboxMessage;
import com.nyihtuun.bentosystem.invoiceservice.application_service.ports.output.message.InvoiceEventMessagePublisher;
import com.nyihtuun.bentosystem.invoiceservice.application_service.ports.output.repository.InvoiceEventOutboxRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.function.BiConsumer;

import static com.nyihtuun.bentosystem.invoiceservice.InvoiceConstants.SCHEDULER_FIXED_RATE;
import static com.nyihtuun.bentosystem.invoiceservice.InvoiceConstants.SCHEDULER_INITIAL_DELAY;

@Slf4j
@AllArgsConstructor
@Component
public class InvoiceEventOutboxScheduler {

    private final InvoiceEventOutboxRepository invoiceEventOutboxRepository;
    private final InvoiceEventMessagePublisher invoiceEventMessagePublisher;

    @Scheduled(fixedDelayString = SCHEDULER_FIXED_RATE, initialDelayString = SCHEDULER_INITIAL_DELAY)
    @Transactional
    public void processOutboxMessages() {
        log.info("Processing invoice outbox messages.");
        invoiceEventOutboxRepository.findByOutboxStatus(OutboxStatus.STARTED)
                                    .ifPresent(outboxMessages -> {
                                        log.info("Found {} invoice outbox messages to process.", outboxMessages.size());
                                        outboxMessages.forEach(outboxMessage -> {
                                            log.info("Publishing InvoiceEventOutboxMessage: {}", outboxMessage.getId());
                                            invoiceEventMessagePublisher.publish(outboxMessage, getCallback(outboxMessage));
                                        });
                                    });
    }

    private BiConsumer<SendResult<String, byte[]>, Throwable> getCallback(InvoiceEventOutboxMessage outboxMessage) {
        return (sendResult, throwable) -> {
            if (throwable == null) {
                RecordMetadata metadata = sendResult.getRecordMetadata();
                log.info("Received new metadata from Kafka for InvoiceEventOutboxMessage: {}. Topic: {}; Partition {}; Offset {}; Timestamp {}",
                         outboxMessage.getId(),
                         metadata.topic(),
                         metadata.partition(),
                         metadata.offset(),
                         metadata.timestamp());

                changeOutboxStatusAndPersist(outboxMessage, OutboxStatus.COMPLETED);
            } else {
                log.error("Error occurred while publishing InvoiceEventOutboxMessage: {}", outboxMessage.getId(), throwable);
                changeOutboxStatusAndPersist(outboxMessage, OutboxStatus.FAILED);
            }
        };
    }

    private void changeOutboxStatusAndPersist(InvoiceEventOutboxMessage outboxMessage, OutboxStatus outboxStatus) {
        outboxMessage.setOutboxStatus(outboxStatus);
        outboxMessage.setProcessedAt(Instant.now());
        invoiceEventOutboxRepository.save(outboxMessage);
        log.info("InvoiceEventOutboxMessage with id {} marked as {}", outboxMessage.getId(), outboxStatus);
    }
}
