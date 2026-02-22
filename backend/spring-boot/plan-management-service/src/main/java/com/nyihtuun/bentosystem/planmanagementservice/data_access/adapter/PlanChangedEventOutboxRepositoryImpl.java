package com.nyihtuun.bentosystem.planmanagementservice.data_access.adapter;

import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.output.repository.PlanChangedEventOutboxRepository;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_entity.PlanChangedEventOutboxEntity;
import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.jpa_repository.PlanChangedEventOutboxJpaRepository;
import com.nyihtuun.bentosystem.planmanagementservice.data_access.mapper.PlanChangedEventOutboxDataAccessMapper;
import com.nyihtuun.bentosystem.planmanagementservice.application_service.outbox.model.PlanOutboxMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@AllArgsConstructor
@Component
public class PlanChangedEventOutboxRepositoryImpl implements PlanChangedEventOutboxRepository {

    private final PlanChangedEventOutboxJpaRepository planChangedEventOutboxJpaRepository;
    private final PlanChangedEventOutboxDataAccessMapper mapper;

    @Override
    public void save(PlanOutboxMessage planOutboxMessage) {
        PlanChangedEventOutboxEntity planChangedEventOutboxEntity = mapper.outboxMessageToOutboxEntity(planOutboxMessage);
        mapper.outboxEntityToOutboxMessage(planChangedEventOutboxJpaRepository.save(planChangedEventOutboxEntity));
    }

    @Override
    public List<PlanOutboxMessage> findByOutboxStatus(OutboxStatus outboxStatus) {
        return planChangedEventOutboxJpaRepository.findAllByOutboxStatus(outboxStatus)
                                                  .stream()
                                                  .map(mapper::outboxEntityToOutboxMessage)
                                                  .toList();

    }

    @Override
    public void deleteByOutboxStatus(OutboxStatus outboxStatus) {
        planChangedEventOutboxJpaRepository.deleteAll(planChangedEventOutboxJpaRepository.findAllByOutboxStatus(outboxStatus));
    }
}
