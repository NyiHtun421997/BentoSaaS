package com.nyihtuun.bentosystem.subscriptionservice.data_access.mapper;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.outbox.model.UserPlanSubscriptionEventOutboxMessage;
import com.nyihtuun.bentosystem.subscriptionservice.data_access.jpa_entity.UserPlanSubscriptionEventOutboxEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import subscription.events.UserPlanSubscriptionEvent;

@Slf4j
@Component
public class UserPlanSubscriptionEventOutboxDataAccessMapper {

    public UserPlanSubscriptionEventOutboxEntity outboxMessageToOutboxEntity(UserPlanSubscriptionEventOutboxMessage userPlanSubscriptionEventOutboxMessage) {
        return UserPlanSubscriptionEventOutboxEntity.builder()
                                           .id(userPlanSubscriptionEventOutboxMessage.getId())
                                           .createdAt(userPlanSubscriptionEventOutboxMessage.getCreatedAt())
                                           .processedAt(userPlanSubscriptionEventOutboxMessage.getProcessedAt())
                                           .payload(writePayloadAsString(userPlanSubscriptionEventOutboxMessage.getPayload()))
                                           .outboxStatus(userPlanSubscriptionEventOutboxMessage.getOutboxStatus())
                                           .version(userPlanSubscriptionEventOutboxMessage.getVersion())
                                           .build();
    }

    public UserPlanSubscriptionEventOutboxMessage outboxEntityToOutboxMessage(UserPlanSubscriptionEventOutboxEntity userPlanSubscriptionEventOutboxEntity) {
        return UserPlanSubscriptionEventOutboxMessage.builder()
                                            .id(userPlanSubscriptionEventOutboxEntity.getId())
                                            .createdAt(userPlanSubscriptionEventOutboxEntity.getCreatedAt())
                                            .processedAt(userPlanSubscriptionEventOutboxEntity.getProcessedAt())
                                            .payload(readPayloadFromString(userPlanSubscriptionEventOutboxEntity.getPayload()))
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
            log.error("Could not write UserPlanSubscriptionEvent as string!", e);
            throw new RuntimeException("Could not write UserPlanSubscriptionEvent as string!", e);
        }
    }

    private UserPlanSubscriptionEvent readPayloadFromString(String payload) {
        try {
            UserPlanSubscriptionEvent.Builder builder = UserPlanSubscriptionEvent.newBuilder();
            JsonFormat.parser()
                      .ignoringUnknownFields()
                      .merge(payload, builder);
            return builder.build();
        } catch (Exception e) {
            log.error("Could not read UserPlanSubscriptionEvent from string!", e);
            throw new RuntimeException("Could not read UserPlanSubscriptionEvent from string!", e);
        }
    }
}
