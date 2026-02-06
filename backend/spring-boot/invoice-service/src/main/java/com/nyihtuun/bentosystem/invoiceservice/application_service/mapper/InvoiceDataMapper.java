package com.nyihtuun.bentosystem.invoiceservice.application_service.mapper;

import com.nyihtuun.bentosystem.invoiceservice.application_service.dto.response.InvoiceResponseDto;
import com.nyihtuun.bentosystem.invoiceservice.domain.entity.Invoice;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

import static com.nyihtuun.bentosystem.invoiceservice.InvoiceConstants.ASIA_TOKYO_ZONE;

@Component
public class InvoiceDataMapper {

    public InvoiceResponseDto mapInvoiceToInvoiceResponseDto(Invoice invoice) {
        return InvoiceResponseDto.builder()
                .invoiceId(invoice.getId().getValue())
                .subscriptionId(invoice.getSubscriptionId().getValue())
                .userId(invoice.getUserId().getValue())
                .providedUserId(invoice.getProvidedUserId().getValue())
                .invoiceStatus(invoice.getInvoiceStatus())
                .amount(invoice.getAmount().amount())
                .issuedAt(invoice.getIssuedAt() != null ? invoice.getIssuedAt().atZone(ZoneId.of(ASIA_TOKYO_ZONE)) : null)
                .updatedAt(invoice.getUpdatedAt() != null ? invoice.getUpdatedAt().atZone(ZoneId.of(ASIA_TOKYO_ZONE)) : null)
                .paidAt(invoice.getPaidAt() != null ? invoice.getPaidAt().atZone(ZoneId.of(ASIA_TOKYO_ZONE)) : null)
                .periodStart(invoice.getPeriodStart())
                .periodEnd(invoice.getPeriodEnd())
                .build();
    }
}
