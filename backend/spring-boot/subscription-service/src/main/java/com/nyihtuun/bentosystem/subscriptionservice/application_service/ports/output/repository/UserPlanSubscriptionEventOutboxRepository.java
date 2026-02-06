package com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.repository;


import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.outbox.model.UserPlanSubscriptionEventOutboxMessage;

import java.util.List;

public interface UserPlanSubscriptionEventOutboxRepository {
    void save(UserPlanSubscriptionEventOutboxMessage userPlanSubscriptionEventOutboxMessage);
    List<UserPlanSubscriptionEventOutboxMessage> findByOutboxStatus(OutboxStatus outboxStatus);
    void deleteByOutboxStatus(OutboxStatus outboxStatus);
}
