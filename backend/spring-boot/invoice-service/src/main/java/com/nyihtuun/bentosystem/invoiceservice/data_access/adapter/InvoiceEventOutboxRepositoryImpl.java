package com.nyihtuun.bentosystem.invoiceservice.data_access.adapter;

import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import com.nyihtuun.bentosystem.invoiceservice.application_service.outbox.model.InvoiceEventOutboxMessage;
import com.nyihtuun.bentosystem.invoiceservice.application_service.ports.output.repository.InvoiceEventOutboxRepository;
import com.nyihtuun.bentosystem.invoiceservice.data_access.jpa_repository.InvoiceEventOutboxJpaRepository;
import com.nyihtuun.bentosystem.invoiceservice.data_access.mapper.InvoiceEventOutboxDataAccessMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class InvoiceEventOutboxRepositoryImpl implements InvoiceEventOutboxRepository {

    private final InvoiceEventOutboxJpaRepository invoiceEventOutboxJpaRepository;
    private final InvoiceEventOutboxDataAccessMapper invoiceEventOutboxDataAccessMapper;

    @Override
    public InvoiceEventOutboxMessage save(InvoiceEventOutboxMessage invoiceEventOutboxMessage) {
        return invoiceEventOutboxDataAccessMapper.outboxEntityToOutboxMessage(
                invoiceEventOutboxJpaRepository.save(
                        invoiceEventOutboxDataAccessMapper.outboxMessageToOutboxEntity(invoiceEventOutboxMessage)
                )
        );
    }

    @Override
    public Optional<List<InvoiceEventOutboxMessage>> findByOutboxStatus(OutboxStatus status) {
        return invoiceEventOutboxJpaRepository.findByOutboxStatus(status)
                                              .map(list -> list.stream()
                                                               .map(invoiceEventOutboxDataAccessMapper::outboxEntityToOutboxMessage)
                                                               .toList());
    }

    @Override
    public void deleteByOutboxStatus(OutboxStatus status) {
        invoiceEventOutboxJpaRepository.deleteByOutboxStatus(status);
    }
}
