package com.nyihtuun.bentosystem.invoiceservice.application_service.impl.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import com.nyihtuun.bentosystem.domain.event.EventCreationFunction;
import com.nyihtuun.bentosystem.invoiceservice.domain.entity.Invoice;
import invoice.notification_events.PlanManagementNotificationEvent;
import lombok.extern.slf4j.Slf4j;

import static com.nyihtuun.bentosystem.domain.utility.CommonConstants.INVOICE_ISSUED_NOTIFICATION_EVENT;

@Slf4j
public class InvoiceEventHelper {

    public static final EventCreationFunction<Invoice, String, ObjectNode, PlanManagementNotificationEvent>
            INVOICE_NOTIFICATION_EVENT_CREATION_FUNCTION = (invoice, planId, payload) -> {
        return PlanManagementNotificationEvent.newBuilder()
                                              .setUserId(invoice.getUserId().getValue().toString())
                                              .setPlanId(planId)
                                              .setNotificationEventType(INVOICE_ISSUED_NOTIFICATION_EVENT)
                                              .setPayload(constructPayload(payload))
                                              .build();
    };

    private static Struct constructPayload(ObjectNode payload) {
        Struct.Builder builder = Struct.newBuilder();
        try {
            JsonFormat.parser().merge(payload.toString(), builder);
        } catch (InvalidProtocolBufferException e) {
            log.error("Could not construct payload from JSON!", e);
            throw new RuntimeException(e);
        }
        return builder.build();
    }
}
