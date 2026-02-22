package com.nyihtuun.bentosystem.subscriptionservice.data_access.mapper;

import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import subscription.notification_events.PlanManagementNotificationEvent;

import static com.nyihtuun.bentosystem.domain.utility.CommonConstants.NOTIFICATION;

@Slf4j
@Component(NOTIFICATION)
public class PlanNotificationEventOutboxPayloadReader implements OutboxPayloadReader {
    @Override
    public PlanManagementNotificationEvent read(String payload) {
        try {
            PlanManagementNotificationEvent.Builder builder = PlanManagementNotificationEvent.newBuilder();
            JsonFormat.parser()
                      .ignoringUnknownFields()
                      .merge(payload, builder);
            return builder.build();
        } catch (Exception e) {
            log.error("Could not read PlanManagementNotificationEvent from string!", e);
            throw new RuntimeException("Could not read PlanManagementNotificationEvent from string!", e);
        }
    }
}