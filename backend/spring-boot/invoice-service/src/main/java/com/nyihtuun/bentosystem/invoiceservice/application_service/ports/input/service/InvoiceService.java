package com.nyihtuun.bentosystem.invoiceservice.application_service.ports.input.service;

import com.nyihtuun.bentosystem.domain.valueobject.status.InvoiceStatus;
import com.nyihtuun.bentosystem.invoiceservice.application_service.dto.response.InvoiceResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceService {
    Optional<InvoiceResponseDto> getInvoiceById(UUID invoiceId);
    List<InvoiceResponseDto> getMyInvoicesByDate(UUID userId, LocalDate date);
    InvoiceResponseDto makePayment(UUID invoiceId);
    InvoiceResponseDto updateInvoiceStatus(UUID invoiceId, InvoiceStatus invoiceStatus);
}
