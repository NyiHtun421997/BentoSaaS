package com.nyihtuun.bentosystem.subscriptionservice.application_service.outbox.scheduler;

import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.outbox.model.UserPlanSubscriptionEventOutboxMessage;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.message.UserPlanSubscriptionMessagePublisher;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.repository.UserPlanSubscriptionEventOutboxRepository;
import com.nyihtuun.bentosystem.subscriptionservice.data_access.jpa_entity.UserPlanSubscriptionEventOutboxEntity;
import com.nyihtuun.bentosystem.subscriptionservice.data_access.mapper.UserPlanSubscriptionEventOutboxDataAccessMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.function.BiConsumer;

import static com.nyihtuun.bentosystem.subscriptionservice.SubscriptionConstants.SCHEDULER_FIXED_RATE;
import static com.nyihtuun.bentosystem.subscriptionservice.SubscriptionConstants.SCHEDULER_INITIAL_DELAY;

@Slf4j
@AllArgsConstructor
@Component
public class UserPlanSubscriptionEventOutboxScheduler {

    private final UserPlanSubscriptionEventOutboxRepository userPlanSubscriptionEventOutboxRepository;
    private final UserPlanSubscriptionMessagePublisher userPlanSubscriptionMessagePublisher;
    private final UserPlanSubscriptionEventOutboxDataAccessMapper mapper;

    @Scheduled(fixedDelayString = SCHEDULER_FIXED_RATE, initialDelayString = SCHEDULER_INITIAL_DELAY)
    public void processOutboxMessages() {
        log.debug("Processing outbox messages.");
        List<UserPlanSubscriptionEventOutboxMessage> userPlanSubscriptionEventOutboxMessages = userPlanSubscriptionEventOutboxRepository.findByOutboxStatus(
                OutboxStatus.STARTED);
        userPlanSubscriptionEventOutboxMessages.forEach(userPlanSubscriptionEventOutboxMessage ->
                                                        {
                                                            log.info("Publishing UserPlanSubscriptionEvent: {}",
                                                                     userPlanSubscriptionEventOutboxMessage);
                                                            userPlanSubscriptionMessagePublisher.publish(
                                                                    userPlanSubscriptionEventOutboxMessage,
                                                                    getCallback(userPlanSubscriptionEventOutboxMessage));
                                                        });
    }

    private BiConsumer<SendResult<String, byte[]>, Throwable> getCallback(UserPlanSubscriptionEventOutboxMessage userPlanSubscriptionEventOutboxMessage) {
        return (sendResult, throwable) -> {
            if (throwable == null) {
                RecordMetadata metadata = sendResult.getRecordMetadata();
                log.info(
                        "Received new metadata from Kafka for UserPlanSubscriptionEvent: {}. Topic: {}; Partition {}; Offset {}; Timestamp {}, at time {}",
                        userPlanSubscriptionEventOutboxMessage,
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset(),
                        metadata.timestamp(),
                        System.nanoTime());

                changeOutboxStatusAndPersist(userPlanSubscriptionEventOutboxMessage, OutboxStatus.COMPLETED);
            } else {
                log.error("Error occurred while publishing UserPlanSubscriptionEvent: {}",
                          userPlanSubscriptionEventOutboxMessage,
                          throwable);
                changeOutboxStatusAndPersist(userPlanSubscriptionEventOutboxMessage, OutboxStatus.FAILED);
            }
        };
    }

    private void changeOutboxStatusAndPersist(UserPlanSubscriptionEventOutboxMessage userPlanSubscriptionEventOutboxMessage,
                                              OutboxStatus outboxStatus) {
        UserPlanSubscriptionEventOutboxEntity userPlanSubscriptionEventOutboxEntity = mapper.outboxMessageToOutboxEntity(
                userPlanSubscriptionEventOutboxMessage);

        userPlanSubscriptionEventOutboxEntity.setOutboxStatus(outboxStatus);
        userPlanSubscriptionEventOutboxEntity.setProcessedAt(Instant.now());

        log.info("UserPlanSubscriptionEventOutboxEntity with id {} marked as {}",
                 userPlanSubscriptionEventOutboxEntity.getId(),
                 outboxStatus);
        userPlanSubscriptionEventOutboxRepository.save(mapper.outboxEntityToOutboxMessage(userPlanSubscriptionEventOutboxEntity));
        log.info("UserPlanSubscriptionEventOutboxEntity with id {} saved", userPlanSubscriptionEventOutboxEntity.getId());
    }
}
