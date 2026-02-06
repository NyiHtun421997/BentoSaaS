package com.nyihtuun.bentosystem.domain.messaging;

import com.google.protobuf.Message;
import com.nyihtuun.bentosystem.domain.event.DomainEvent;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Slf4j
public abstract class AbstractMessageProducer <T extends BaseMessagingModel> {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final String topicName;

    public AbstractMessageProducer(KafkaTemplate<String, byte[]> kafkaTemplate, String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public void publish(T event, BiConsumer<? super SendResult<String, byte[]>, ? super Throwable> callback) {
        Message payload = event.getPayload();

        try {
            CompletableFuture<SendResult<String, byte[]>> completableFuture = kafkaTemplate.send(topicName,
                                                                                                 payload.toByteArray());

            completableFuture.whenComplete(callback);
        } catch (Exception e) {
            log.error("Failed to send message to Kafka topic: {}, event: {}", topicName, event, e);
        }
    }

    @PreDestroy
    public void close() {
        if (kafkaTemplate != null) {
            log.info("Closing Kafka producer!");
            kafkaTemplate.destroy();
        }
    }
}
