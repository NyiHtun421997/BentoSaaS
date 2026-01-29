package com.nyihtuun.bentosystem.subscriptionservice.message.adapter.publisher;

import com.nyihtuun.bentosystem.domain.messaging.AbstractMessageProducer;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.outbox.model.UserPlanSubscriptionEventOutboxMessage;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.message.UserPlanSubscriptionMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.nyihtuun.bentosystem.subscriptionservice.SubscriptionConstants.SUBSCRIPTION_USER_SUBSCRIPTION_TOPIC_NAME;

@Slf4j
@Component
public class UserPlanSubscriptionMessageKafkaPublisherImpl extends AbstractMessageProducer<UserPlanSubscriptionEventOutboxMessage> implements UserPlanSubscriptionMessagePublisher {

    @Autowired
    public UserPlanSubscriptionMessageKafkaPublisherImpl(KafkaTemplate<String, byte[]> kafkaTemplate,
                                                         @Value(SUBSCRIPTION_USER_SUBSCRIPTION_TOPIC_NAME) String topicName) {
        super(kafkaTemplate, topicName);
    }
}
