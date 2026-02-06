package com.nyihtuun.bentosystem.invoiceservice.data_access.adapter;

import com.nyihtuun.bentosystem.invoiceservice.application_service.ports.output.repository.InvoiceRepository;
import com.nyihtuun.bentosystem.invoiceservice.data_access.jpa_entity.InvoiceEntity;
import com.nyihtuun.bentosystem.invoiceservice.data_access.jpa_repository.InvoiceJpaRepository;
import com.nyihtuun.bentosystem.invoiceservice.data_access.mapper.InvoiceDataAccessMapper;
import com.nyihtuun.bentosystem.invoiceservice.domain.entity.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class InvoiceRepositoryImpl implements InvoiceRepository {

    private final InvoiceJpaRepository invoiceJpaRepository;
    private final InvoiceDataAccessMapper mapper;

    @Autowired
    public InvoiceRepositoryImpl(InvoiceJpaRepository invoiceJpaRepository, InvoiceDataAccessMapper mapper) {
        this.invoiceJpaRepository = invoiceJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Invoice> findByInvoiceId(UUID invoiceId) {
        return invoiceJpaRepository.findById(invoiceId).map(mapper::invoiceEntityToInvoice);
    }

    @Override
    public List<Invoice> findByUserIdAndDate(UUID userId, LocalDate date) {
        return invoiceJpaRepository.findAllByUserIdAndIssuedAtAfter(userId, date.atStartOfDay(ZoneOffset.UTC).toInstant())
                                   .stream()
                                   .map(mapper::invoiceEntityToInvoice)
                                   .toList();
    }

    @Override
    public Invoice save(Invoice invoice) {
        InvoiceEntity saved = invoiceJpaRepository.save(mapper.invoiceToInvoiceEntity(invoice));
        return mapper.invoiceEntityToInvoice(saved);
    }
}
