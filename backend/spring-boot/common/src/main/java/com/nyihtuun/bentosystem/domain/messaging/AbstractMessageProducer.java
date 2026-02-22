package com.nyihtuun.bentosystem.domain.messaging;

import com.google.protobuf.Message;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Slf4j
public abstract class AbstractMessageProducer <T extends BaseMessagingModel> {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public AbstractMessageProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(T event, BiConsumer<? super SendResult<String, byte[]>, ? super Throwable> callback) {
        Message payload = event.getPayload();

        try {
            CompletableFuture<SendResult<String, byte[]>> completableFuture = kafkaTemplate.send(event.getTopicName(),
                                                                                                 event.getUserId().toString(),
                                                                                                 payload.toByteArray());

            completableFuture.whenComplete(callback);
        } catch (Exception e) {
            log.error("Failed to send message to Kafka topic: {}, event: {}", event.getTopicName(), event, e);
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
