package com.nyihtuun.bentosystem.invoiceservice.data_access.mapper;

import com.nyihtuun.bentosystem.domain.valueobject.InvoiceId;
import com.nyihtuun.bentosystem.domain.valueobject.Money;
import com.nyihtuun.bentosystem.domain.valueobject.SubscriptionId;
import com.nyihtuun.bentosystem.domain.valueobject.UserId;
import com.nyihtuun.bentosystem.invoiceservice.data_access.jpa_entity.InvoiceEntity;
import com.nyihtuun.bentosystem.invoiceservice.domain.entity.Invoice;
import org.springframework.stereotype.Component;

@Component
public class InvoiceDataAccessMapper {

    public InvoiceEntity invoiceToInvoiceEntity(Invoice invoice) {
        return InvoiceEntity.builder()
                            .id(invoice.getId().getValue())
                            .subscriptionId(invoice.getSubscriptionId().getValue())
                            .userId(invoice.getUserId().getValue())
                            .providedUserId(invoice.getProvidedUserId().getValue())
                            .invoiceStatus(invoice.getInvoiceStatus())
                            .amount(invoice.getAmount().amount())
                            .issuedAt(invoice.getIssuedAt())
                            .updatedAt(invoice.getUpdatedAt())
                            .paidAt(invoice.getPaidAt())
                            .periodStart(invoice.getPeriodStart())
                            .periodEnd(invoice.getPeriodEnd())
                            .build();
    }

    public Invoice invoiceEntityToInvoice(InvoiceEntity invoiceEntity) {
        return Invoice.builder()
                      .invoiceId(new InvoiceId(invoiceEntity.getId()))
                      .subscriptionId(new SubscriptionId(invoiceEntity.getSubscriptionId()))
                      .userId(new UserId(invoiceEntity.getUserId()))
                      .providedUserId(new UserId(invoiceEntity.getProvidedUserId()))
                      .invoiceStatus(invoiceEntity.getInvoiceStatus())
                      .amount(new Money(invoiceEntity.getAmount()))
                      .issuedAt(invoiceEntity.getIssuedAt())
                      .updatedAt(invoiceEntity.getUpdatedAt())
                      .paidAt(invoiceEntity.getPaidAt())
                      .periodStart(invoiceEntity.getPeriodStart())
                      .periodEnd(invoiceEntity.getPeriodEnd())
                      .build();
    }
}
