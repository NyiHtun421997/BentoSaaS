package com.nyihtuun.bentosystem.invoiceservice.application_service.ports.output.message;

import com.nyihtuun.bentosystem.invoiceservice.application_service.outbox.model.InvoiceEventOutboxMessage;
import org.springframework.kafka.support.SendResult;

import java.util.function.BiConsumer;

public interface InvoiceEventMessagePublisher {
    void publish(InvoiceEventOutboxMessage invoiceEventOutboxMessage,
                 BiConsumer<? super SendResult<String, byte[]>, ? super Throwable> callback);
}
