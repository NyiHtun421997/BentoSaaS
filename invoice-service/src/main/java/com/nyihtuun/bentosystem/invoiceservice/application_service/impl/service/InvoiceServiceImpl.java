package com.nyihtuun.bentosystem.invoiceservice.application_service.impl.service;

import com.nyihtuun.bentosystem.invoiceservice.application_service.dto.response.InvoiceResponseDto;
import com.nyihtuun.bentosystem.invoiceservice.application_service.mapper.InvoiceDataMapper;
import com.nyihtuun.bentosystem.invoiceservice.application_service.ports.input.service.InvoiceService;
import com.nyihtuun.bentosystem.invoiceservice.application_service.ports.output.repository.InvoiceRepository;
import com.nyihtuun.bentosystem.invoiceservice.domain.entity.Invoice;
import com.nyihtuun.bentosystem.invoiceservice.domain.exception.InvoiceDomainException;
import com.nyihtuun.bentosystem.invoiceservice.domain.exception.InvoiceErrorCode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@AllArgsConstructor
@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceDataMapper invoiceDataMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<InvoiceResponseDto> getInvoiceById(UUID invoiceId) {
        return invoiceRepository.findByInvoiceId(invoiceId)
                .map(invoiceDataMapper::mapInvoiceToInvoiceResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponseDto> getMyInvoicesByDate(UUID userId, LocalDate date) {
        return invoiceRepository.findByUserIdAndDate(userId, date)
                .stream()
                .map(invoiceDataMapper::mapInvoiceToInvoiceResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public InvoiceResponseDto makePayment(UUID invoiceId) {
        log.info("Making payment for invoice with id: {}", invoiceId);
        Invoice invoice = findInvoiceById(invoiceId);

        invoice.pay();
        log.info("Invoice with id: {} is paid", invoiceId);
        Invoice saved = invoiceRepository.save(invoice);
        return invoiceDataMapper.mapInvoiceToInvoiceResponseDto(saved);
    }

    @Override
    @Transactional
    public InvoiceResponseDto cancelPayment(UUID invoiceId) {
        log.info("Cancelling payment for invoice with id: {}", invoiceId);
        Invoice invoice = findInvoiceById(invoiceId);

        invoice.cancel();
        log.info("Invoice with id: {} is cancelled", invoiceId);
        Invoice saved = invoiceRepository.save(invoice);
        return invoiceDataMapper.mapInvoiceToInvoiceResponseDto(saved);
    }

    @Override
    @Transactional
    public InvoiceResponseDto markPaymentFailed(UUID invoiceId) {
        log.info("Marking payment for invoice with id: {} as failed", invoiceId);
        Invoice invoice = findInvoiceById(invoiceId);

        invoice.markFailed();
        log.info("Invoice with id: {} is marked as failed", invoiceId);
        Invoice saved = invoiceRepository.save(invoice);
        return invoiceDataMapper.mapInvoiceToInvoiceResponseDto(saved);
    }

    private Invoice findInvoiceById(UUID invoiceId) {
        return invoiceRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> {
                    log.error("Invoice with id: {} not found", invoiceId);
                    return new InvoiceDomainException(InvoiceErrorCode.INVOICE_NOT_FOUND);
                });
    }
}
