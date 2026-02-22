package com.nyihtuun.bentosystem.invoiceservice.application_service.outbox.model;

import com.google.protobuf.Message;
import com.nyihtuun.bentosystem.domain.messaging.BaseMessagingModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@ToString
public class InvoiceEventOutboxMessage extends BaseMessagingModel {
    private Message payload;
}
