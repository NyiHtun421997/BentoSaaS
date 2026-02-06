package com.nyihtuun.bentosystem.planmanagementservice.message.adapter.publisher;

import com.nyihtuun.bentosystem.domain.messaging.AbstractMessageProducer;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.outbox.model.PlanChangedEventOutboxMessage;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.message.PlanChangedMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.nyihtuun.bentosystem.planmanagementservice.PlanManagementConstants.PLAN_MANAGEMENT_PLAN_CHANGED_TOPIC_NAME;

@Slf4j
@Component
public class PlanChangedMessageKafkaPublisherImpl extends AbstractMessageProducer<PlanChangedEventOutboxMessage> implements PlanChangedMessagePublisher {

    @Autowired
    public PlanChangedMessageKafkaPublisherImpl(KafkaTemplate<String, byte[]> kafkaTemplate,
                                                @Value(PLAN_MANAGEMENT_PLAN_CHANGED_TOPIC_NAME) String topicName) {
        super(kafkaTemplate, topicName);
    }
}
