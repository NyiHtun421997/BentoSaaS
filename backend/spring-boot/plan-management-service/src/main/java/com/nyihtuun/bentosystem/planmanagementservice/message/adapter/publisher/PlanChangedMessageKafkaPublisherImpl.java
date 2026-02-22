package com.nyihtuun.bentosystem.planmanagementservice.message.adapter.publisher;

import com.nyihtuun.bentosystem.domain.messaging.AbstractMessageProducer;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.outbox.model.PlanOutboxMessage;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.message.PlanChangedMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlanChangedMessageKafkaPublisherImpl extends AbstractMessageProducer<PlanOutboxMessage> implements PlanChangedMessagePublisher {

    @Autowired
    public PlanChangedMessageKafkaPublisherImpl(KafkaTemplate<String, byte[]> kafkaTemplate) {
        super(kafkaTemplate);
    }
}
