package com.nyihtuun.bentosystem.invoiceservice.message.adapter.publisher;

import com.nyihtuun.bentosystem.domain.messaging.AbstractMessageProducer;
import com.nyihtuun.bentosystem.invoiceservice.application_service.outbox.model.InvoiceEventOutboxMessage;
import com.nyihtuun.bentosystem.invoiceservice.application_service.ports.output.message.InvoiceEventMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InvoiceEventMessageKafkaPublisherImpl extends AbstractMessageProducer<InvoiceEventOutboxMessage> implements InvoiceEventMessagePublisher {

    @Autowired
    public InvoiceEventMessageKafkaPublisherImpl(KafkaTemplate<String, byte[]> kafkaTemplate) {
        super(kafkaTemplate);
    }
}
