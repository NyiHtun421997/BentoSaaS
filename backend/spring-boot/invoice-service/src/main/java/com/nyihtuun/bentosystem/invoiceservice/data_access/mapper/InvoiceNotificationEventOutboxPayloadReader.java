package com.nyihtuun.bentosystem.invoiceservice.data_access.mapper;

import com.google.protobuf.util.JsonFormat;
import invoice.notification_events.PlanManagementNotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.nyihtuun.bentosystem.domain.utility.CommonConstants.NOTIFICATION;

@Slf4j
@Component(NOTIFICATION)
public class InvoiceNotificationEventOutboxPayloadReader implements OutboxPayloadReader {
    @Override
    public PlanManagementNotificationEvent read(String payload) {
        try {
            PlanManagementNotificationEvent.Builder builder = PlanManagementNotificationEvent.newBuilder();
            JsonFormat.parser()
                      .ignoringUnknownFields()
                      .merge(payload, builder);
            return builder.build();
        } catch (Exception e) {
            log.error("Could not read InvoiceNotificationEvent from string!", e);
            throw new RuntimeException("Could not read InvoiceNotificationEvent from string!", e);
        }
    }
}
