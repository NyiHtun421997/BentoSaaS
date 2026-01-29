package com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository;

import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.outbox.model.PlanChangedEventOutboxMessage;

import java.util.List;

public interface PlanChangedEventOutboxRepository {
    void save(PlanChangedEventOutboxMessage planChangedEventOutboxMessage);
    List<PlanChangedEventOutboxMessage> findByOutboxStatus(OutboxStatus outboxStatus);
    void deleteByOutboxStatus(OutboxStatus outboxStatus);
}
