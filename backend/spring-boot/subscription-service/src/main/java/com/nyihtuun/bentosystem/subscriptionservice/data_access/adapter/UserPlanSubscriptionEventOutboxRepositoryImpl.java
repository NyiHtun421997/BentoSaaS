package com.nyihtuun.bentosystem.subscriptionservice.data_access.adapter;

import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.outbox.model.UserPlanSubscriptionEventOutboxMessage;
import com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.repository.UserPlanSubscriptionEventOutboxRepository;
import com.nyihtuun.bentosystem.subscriptionservice.data_access.jpa_entity.UserPlanSubscriptionEventOutboxEntity;
import com.nyihtuun.bentosystem.subscriptionservice.data_access.jpa_repository.UserPlanSubscriptionEventOutboxJpaRepository;
import com.nyihtuun.bentosystem.subscriptionservice.data_access.mapper.UserPlanSubscriptionEventOutboxDataAccessMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@AllArgsConstructor
@Component
public class UserPlanSubscriptionEventOutboxRepositoryImpl implements UserPlanSubscriptionEventOutboxRepository {

    private final UserPlanSubscriptionEventOutboxJpaRepository userPlanSubscriptionEventOutboxJpaRepository;
    private final UserPlanSubscriptionEventOutboxDataAccessMapper mapper;

    @Override
    public void save(UserPlanSubscriptionEventOutboxMessage userPlanSubscriptionEventOutboxMessage) {
        UserPlanSubscriptionEventOutboxEntity userPlanSubscriptionEventOutboxEntity = mapper.outboxMessageToOutboxEntity(userPlanSubscriptionEventOutboxMessage);
        userPlanSubscriptionEventOutboxJpaRepository.save(userPlanSubscriptionEventOutboxEntity);
    }

    @Override
    public List<UserPlanSubscriptionEventOutboxMessage> findByOutboxStatus(OutboxStatus outboxStatus) {
        return userPlanSubscriptionEventOutboxJpaRepository.findAllByOutboxStatus(outboxStatus)
                                                   .stream()
                                                   .map(mapper::outboxEntityToOutboxMessage)
                                                   .toList();
    }

    @Override
    public void deleteByOutboxStatus(OutboxStatus outboxStatus) {
        userPlanSubscriptionEventOutboxJpaRepository.deleteAll(userPlanSubscriptionEventOutboxJpaRepository.findAllByOutboxStatus(outboxStatus));
    }
}
