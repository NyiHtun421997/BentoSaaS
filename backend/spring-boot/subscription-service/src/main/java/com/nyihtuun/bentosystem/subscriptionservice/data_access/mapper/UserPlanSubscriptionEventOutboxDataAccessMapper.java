package com.nyihtuun.bentosystem.subscriptionservice.data_access.mapper;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.outbox.model.UserPlanSubscriptionEventOutboxMessage;
import com.nyihtuun.bentosystem.subscriptionservice.data_access.jpa_entity.UserPlanSubscriptionEventOutboxEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class UserPlanSubscriptionEventOutboxDataAccessMapper {

    private final Map<String, OutboxPayloadReader> outboxPayloadReaderMap;

    public UserPlanSubscriptionEventOutboxDataAccessMapper(Map<String, OutboxPayloadReader> outboxPayloadReaderMap) {
        this.outboxPayloadReaderMap = outboxPayloadReaderMap;
    }

    public UserPlanSubscriptionEventOutboxEntity outboxMessageToOutboxEntity(UserPlanSubscriptionEventOutboxMessage userPlanSubscriptionEventOutboxMessage) {
        return UserPlanSubscriptionEventOutboxEntity.builder()
                                           .id(userPlanSubscriptionEventOutboxMessage.getId())
                                           .userId(userPlanSubscriptionEventOutboxMessage.getUserId())
                                           .createdAt(userPlanSubscriptionEventOutboxMessage.getCreatedAt())
                                           .processedAt(userPlanSubscriptionEventOutboxMessage.getProcessedAt())
                                           .type(userPlanSubscriptionEventOutboxMessage.getType())
                                           .payload(writePayloadAsString(userPlanSubscriptionEventOutboxMessage.getPayload()))
                                           .topicName(userPlanSubscriptionEventOutboxMessage.getTopicName())
                                           .outboxStatus(userPlanSubscriptionEventOutboxMessage.getOutboxStatus())
                                           .version(userPlanSubscriptionEventOutboxMessage.getVersion())
                                           .build();
    }

    public UserPlanSubscriptionEventOutboxMessage outboxEntityToOutboxMessage(UserPlanSubscriptionEventOutboxEntity userPlanSubscriptionEventOutboxEntity) {
        OutboxPayloadReader outboxPayloadReader = outboxPayloadReaderMap.get(userPlanSubscriptionEventOutboxEntity.getType());
        if (outboxPayloadReader == null) {
            throw new RuntimeException("No outbox payload reader found for type: " + userPlanSubscriptionEventOutboxEntity.getType());
        }

        Message payload = outboxPayloadReader.read(userPlanSubscriptionEventOutboxEntity.getPayload());

        return UserPlanSubscriptionEventOutboxMessage.builder()
                                            .id(userPlanSubscriptionEventOutboxEntity.getId())
                                            .userId(userPlanSubscriptionEventOutboxEntity.getUserId())
                                            .createdAt(userPlanSubscriptionEventOutboxEntity.getCreatedAt())
                                            .processedAt(userPlanSubscriptionEventOutboxEntity.getProcessedAt())
                                            .topicName(userPlanSubscriptionEventOutboxEntity.getTopicName())
                                            .type(userPlanSubscriptionEventOutboxEntity.getType())
                                            .payload(payload)
                                            .outboxStatus(userPlanSubscriptionEventOutboxEntity.getOutboxStatus())
                                            .version(userPlanSubscriptionEventOutboxEntity.getVersion())
                                            .build();
    }

    private String writePayloadAsString(Message payload) {
        try {
            // Protobuf-supported, stable JSON representation
            return JsonFormat.printer()
                             .omittingInsignificantWhitespace()
                             .print(payload);
        } catch (Exception e) {
            log.error("Could not write payload as string!", e);
            throw new RuntimeException("Could not write payload as string!", e);
        }
    }
}
