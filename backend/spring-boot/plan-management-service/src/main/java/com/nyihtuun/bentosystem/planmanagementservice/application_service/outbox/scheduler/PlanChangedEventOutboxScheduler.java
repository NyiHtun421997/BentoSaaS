package com.nyihtuun.bentosystem.planmanagementservice.application_service.outbox.scheduler;

import com.nyihtuun.bentosystem.planmanagementservice.application_service.outbox.model.PlanOutboxMessage;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.message.PlanChangedMessagePublisher;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository.PlanChangedEventOutboxRepository;
import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.PlanChangedEventOutboxEntity;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.mapper.PlanChangedEventOutboxDataAccessMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.function.BiConsumer;

import static com.nyihtuun.bentosystem.planmanagementservice.PlanManagementConstants.SCHEDULER_FIXED_RATE;
import static com.nyihtuun.bentosystem.planmanagementservice.PlanManagementConstants.SCHEDULER_INITIAL_DELAY;

@Slf4j
@AllArgsConstructor
@Component
public class PlanChangedEventOutboxScheduler {

    private final PlanChangedEventOutboxRepository planChangedEventOutboxRepository;
    private final PlanChangedMessagePublisher planChangedMessagePublisher;
    private final PlanChangedEventOutboxDataAccessMapper mapper;

    @Scheduled(fixedDelayString = SCHEDULER_FIXED_RATE, initialDelayString = SCHEDULER_INITIAL_DELAY)
    public void processOutboxMessages() {
        log.info("Processing outbox messages.");
        List<PlanOutboxMessage> planOutboxMessages = planChangedEventOutboxRepository.findByOutboxStatus(OutboxStatus.STARTED);
        planOutboxMessages.forEach(planChangedEventOutboxMessage ->
                                               {
                                                   log.info("Publishing PlanChangedEvent: {}", planChangedEventOutboxMessage);
                                                   planChangedMessagePublisher.publish(planChangedEventOutboxMessage,
                                                                                       getCallback(planChangedEventOutboxMessage));
                                               });
    }

    private BiConsumer<SendResult<String, byte[]>, Throwable> getCallback(PlanOutboxMessage planOutboxMessage) {
        return (sendResult, throwable) -> {
            if (throwable == null) {
                RecordMetadata metadata = sendResult.getRecordMetadata();
                log.info(
                        "Received new metadata from Kafka for PlanChangedEvent: {}. Topic: {}; Partition {}; Offset {}; Timestamp {}, at time {}",
                        planOutboxMessage,
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset(),
                        metadata.timestamp(),
                        System.nanoTime());

                changeOutboxStatusAndPersist(planOutboxMessage, OutboxStatus.COMPLETED);
            } else {
                log.error("Error occurred while publishing PlanChangedEvent: {}", planOutboxMessage, throwable);
                changeOutboxStatusAndPersist(planOutboxMessage, OutboxStatus.FAILED);
            }
        };
    }

    private void changeOutboxStatusAndPersist(PlanOutboxMessage planOutboxMessage,
                                              OutboxStatus outboxStatus) {
        PlanChangedEventOutboxEntity planChangedEventOutboxEntity = mapper.outboxMessageToOutboxEntity(
                planOutboxMessage);

        planChangedEventOutboxEntity.setOutboxStatus(outboxStatus);
        planChangedEventOutboxEntity.setProcessedAt(Instant.now());

        log.info("PlanChangedEventOutboxEntity with id {} marked as {}", planChangedEventOutboxEntity.getId(), outboxStatus);
        planChangedEventOutboxRepository.save(mapper.outboxEntityToOutboxMessage(planChangedEventOutboxEntity));
        log.info("PlanChangedEventOutboxEntity with id {} saved", planChangedEventOutboxEntity.getId());
    }
}
