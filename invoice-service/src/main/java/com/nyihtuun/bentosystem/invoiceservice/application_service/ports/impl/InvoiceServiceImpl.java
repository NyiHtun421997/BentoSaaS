package com.nyihtuun.bentosystem.invoiceservice.application_service.ports.impl;

import com.nyihtuun.bentosystem.invoiceservice.application_service.ports.input.service.InvoiceService;
import com.nyihtuun.bentosystem.invoiceservice.application_service.ports.output.repository.InvoiceRepository;
import com.nyihtuun.bentosystem.invoiceservice.domain.entity.Invoice;
import com.nyihtuun.bentosystem.invoiceservice.domain.exception.InvoiceDomainException;
import com.nyihtuun.bentosystem.invoiceservice.domain.exception.InvoiceErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Autowired
    public InvoiceServiceImpl(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invoice> getInvoiceById(UUID invoiceId) {
        return invoiceRepository.findByInvoiceId(invoiceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> getMyInvoicesByDate(UUID userId, LocalDate date) {
        return invoiceRepository.findByUserIdAndDate(userId, date);
    }

    @Override
    @Transactional
    public Invoice makePayment(UUID invoiceId) {
        log.info("Making payment for invoice with id: {}", invoiceId);
        Invoice invoice = findInvoiceById(invoiceId);

        invoice.pay();
        log.info("Invoice with id: {} is paid", invoiceId);
        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public Invoice cancelPayment(UUID invoiceId) {
        log.info("Cancelling payment for invoice with id: {}", invoiceId);
        Invoice invoice = findInvoiceById(invoiceId);

        invoice.cancel();
        log.info("Invoice with id: {} is cancelled", invoiceId);
        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public Invoice markPaymentFailed(UUID invoiceId) {
        log.info("Marking payment for invoice with id: {} as failed", invoiceId);
        Invoice invoice = findInvoiceById(invoiceId);

        invoice.markFailed();
        log.info("Invoice with id: {} is marked as failed", invoiceId);
        return invoiceRepository.save(invoice);
    }

    private Invoice findInvoiceById(UUID invoiceId) {
        return invoiceRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> {
                    log.error("Invoice with id: {} not found", invoiceId);
                    return new InvoiceDomainException(InvoiceErrorCode.INVOICE_NOT_FOUND);
                });
    }
}
