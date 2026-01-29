package com.nyihtuun.bentosystem.planmanagementservice.data_access.mapper;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.PlanChangedEventOutboxEntity;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.outbox.model.PlanChangedEventOutboxMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import plan_management.events.PlanChangedEvent;

@Slf4j
@Component
public class PlanChangedEventOutboxDataAccessMapper {

    public PlanChangedEventOutboxEntity outboxMessageToOutboxEntity(PlanChangedEventOutboxMessage planChangedEventOutboxMessage) {
        return PlanChangedEventOutboxEntity.builder()
                                           .id(planChangedEventOutboxMessage.getId())
                                           .createdAt(planChangedEventOutboxMessage.getCreatedAt())
                                           .processedAt(planChangedEventOutboxMessage.getProcessedAt())
                                           .payload(writePayloadAsString(planChangedEventOutboxMessage.getPayload()))
                                           .outboxStatus(planChangedEventOutboxMessage.getOutboxStatus())
                                           .version(planChangedEventOutboxMessage.getVersion())
                                           .build();
    }

    public PlanChangedEventOutboxMessage outboxEntityToOutboxMessage(PlanChangedEventOutboxEntity planChangedEventOutboxEntity) {
        return PlanChangedEventOutboxMessage.builder()
                                            .id(planChangedEventOutboxEntity.getId())
                                            .createdAt(planChangedEventOutboxEntity.getCreatedAt())
                                            .processedAt(planChangedEventOutboxEntity.getProcessedAt())
                                            .payload(readPayloadFromString(planChangedEventOutboxEntity.getPayload()))
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

    private PlanChangedEvent readPayloadFromString(String payload) {
        try {
            PlanChangedEvent.Builder builder = PlanChangedEvent.newBuilder();
            JsonFormat.parser()
                      .ignoringUnknownFields()
                      .merge(payload, builder);
            return builder.build();
        } catch (Exception e) {
            log.error("Could not read PlanChangedEvent from string!", e);
            throw new RuntimeException("Could not read PlanChangedEvent from string!", e);
        }
    }
}
