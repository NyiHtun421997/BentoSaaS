package com.nyihtuun.bentosystem.invoiceservice.data_access.mapper;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.nyihtuun.bentosystem.invoiceservice.application_service.outbox.model.InvoiceEventOutboxMessage;
import com.nyihtuun.bentosystem.invoiceservice.data_access.jpa_entity.InvoiceEventOutboxEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class InvoiceEventOutboxDataAccessMapper {

    private final Map<String, OutboxPayloadReader> outboxPayloadReaderMap;

    public InvoiceEventOutboxDataAccessMapper(Map<String, OutboxPayloadReader> outboxPayloadReaderMap) {
        this.outboxPayloadReaderMap = outboxPayloadReaderMap;
    }

    public InvoiceEventOutboxEntity outboxMessageToOutboxEntity(InvoiceEventOutboxMessage invoiceEventOutboxMessage) {
        return InvoiceEventOutboxEntity.builder()
                                       .id(invoiceEventOutboxMessage.getId())
                                       .userId(invoiceEventOutboxMessage.getUserId())
                                       .createdAt(invoiceEventOutboxMessage.getCreatedAt())
                                       .processedAt(invoiceEventOutboxMessage.getProcessedAt())
                                       .type(invoiceEventOutboxMessage.getType())
                                       .payload(writePayloadAsString(invoiceEventOutboxMessage.getPayload()))
                                       .topicName(invoiceEventOutboxMessage.getTopicName())
                                       .outboxStatus(invoiceEventOutboxMessage.getOutboxStatus())
                                       .version(invoiceEventOutboxMessage.getVersion())
                                       .build();
    }

    public InvoiceEventOutboxMessage outboxEntityToOutboxMessage(InvoiceEventOutboxEntity invoiceEventOutboxEntity) {
        OutboxPayloadReader outboxPayloadReader = outboxPayloadReaderMap.get(invoiceEventOutboxEntity.getType());
        if (outboxPayloadReader == null) {
            throw new RuntimeException("No outbox payload reader found for type: " + invoiceEventOutboxEntity.getType());
        }

        Message payload = outboxPayloadReader.read(invoiceEventOutboxEntity.getPayload());

        return InvoiceEventOutboxMessage.builder()
                                        .id(invoiceEventOutboxEntity.getId())
                                        .userId(invoiceEventOutboxEntity.getUserId())
                                        .createdAt(invoiceEventOutboxEntity.getCreatedAt())
                                        .processedAt(invoiceEventOutboxEntity.getProcessedAt())
                                        .topicName(invoiceEventOutboxEntity.getTopicName())
                                        .type(invoiceEventOutboxEntity.getType())
                                        .payload(payload)
                                        .outboxStatus(invoiceEventOutboxEntity.getOutboxStatus())
                                        .version(invoiceEventOutboxEntity.getVersion())
                                        .build();
    }

    private String writePayloadAsString(Message payload) {
        try {
            return JsonFormat.printer()
                             .omittingInsignificantWhitespace()
                             .print(payload);
        } catch (Exception e) {
            log.error("Could not write payload as string!", e);
            throw new RuntimeException("Could not write payload as string!", e);
        }
    }
}
