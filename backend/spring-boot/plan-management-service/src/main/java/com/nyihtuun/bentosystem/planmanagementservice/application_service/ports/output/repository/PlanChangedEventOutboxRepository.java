package com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository;

import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.outbox.model.PlanOutboxMessage;

import java.util.List;

public interface PlanChangedEventOutboxRepository {
    void save(PlanOutboxMessage planOutboxMessage);
    List<PlanOutboxMessage> findByOutboxStatus(OutboxStatus outboxStatus);
    void deleteByOutboxStatus(OutboxStatus outboxStatus);
}
