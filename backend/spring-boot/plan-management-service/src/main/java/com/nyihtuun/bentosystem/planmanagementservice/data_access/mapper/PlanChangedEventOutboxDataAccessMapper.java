package com.nyihtuun.bentosystem.planmanagementservice.data_access.mapper;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.PlanChangedEventOutboxEntity;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.outbox.model.PlanOutboxMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class PlanChangedEventOutboxDataAccessMapper {

    private final Map<String, OutboxPayloadReader> outboxPayloadReaderMap;

    public PlanChangedEventOutboxDataAccessMapper(Map<String, OutboxPayloadReader> outboxPayloadReaderMap) {
        this.outboxPayloadReaderMap = outboxPayloadReaderMap;
    }

    public PlanChangedEventOutboxEntity outboxMessageToOutboxEntity(PlanOutboxMessage planOutboxMessage) {
        return PlanChangedEventOutboxEntity.builder()
                                           .id(planOutboxMessage.getId())
                                           .userId(planOutboxMessage.getUserId())
                                           .createdAt(planOutboxMessage.getCreatedAt())
                                           .processedAt(planOutboxMessage.getProcessedAt())
                                           .type(planOutboxMessage.getType())
                                           .payload(writePayloadAsString(planOutboxMessage.getPayload()))
                                           .topicName(planOutboxMessage.getTopicName())
                                           .outboxStatus(planOutboxMessage.getOutboxStatus())
                                           .version(planOutboxMessage.getVersion())
                                           .build();
    }

    public PlanOutboxMessage outboxEntityToOutboxMessage(PlanChangedEventOutboxEntity planChangedEventOutboxEntity) {
        OutboxPayloadReader outboxPayloadReader = outboxPayloadReaderMap.get(planChangedEventOutboxEntity.getType());
        if (outboxPayloadReader == null) {
            throw new RuntimeException("No outbox payload reader found for type: " + planChangedEventOutboxEntity.getType());
        }

        Message payload = outboxPayloadReader.read(planChangedEventOutboxEntity.getPayload());

        return PlanOutboxMessage.builder()
                                .id(planChangedEventOutboxEntity.getId())
                                .userId(planChangedEventOutboxEntity.getUserId())
                                .createdAt(planChangedEventOutboxEntity.getCreatedAt())
                                .processedAt(planChangedEventOutboxEntity.getProcessedAt())
                                .topicName(planChangedEventOutboxEntity.getTopicName())
                                .type(planChangedEventOutboxEntity.getType())
                                .payload(payload)
                                .outboxStatus(planChangedEventOutboxEntity.getOutboxStatus())
                                .version(planChangedEventOutboxEntity.getVersion())
                                .build();
    }

    private String writePayloadAsString(Message payload) {
        try {
            // Protobuf-supported, stable JSON representation
            return JsonFormat.printer()
                             .omittingInsignificantWhitespace()
                             .print(payload);
        } catch (Exception e) {
            log.error("Could not write PlanChangedEvent as string!", e);
            throw new RuntimeException("Could not write PlanChangedEvent as string!", e);
        }
    }
}
