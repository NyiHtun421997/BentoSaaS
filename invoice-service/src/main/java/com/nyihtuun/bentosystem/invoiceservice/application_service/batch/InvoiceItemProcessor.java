package com.nyihtuun.bentosystem.invoiceservice.application_service.batch;

import com.nyihtuun.bentosystem.domain.valueobject.Money;
import com.nyihtuun.bentosystem.domain.valueobject.SubscriptionId;
import com.nyihtuun.bentosystem.domain.valueobject.UserId;
import com.nyihtuun.bentosystem.invoiceservice.application_service.InvoiceConstants;
import com.nyihtuun.bentosystem.invoiceservice.data_access.jpa_entity.InvoiceEntity;
import com.nyihtuun.bentosystem.invoiceservice.data_access.mapper.InvoiceDataAccessMapper;
import com.nyihtuun.bentosystem.invoiceservice.domain.entity.Invoice;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component(InvoiceConstants.INVOICE_ITEM_PROCESSOR)
public class InvoiceItemProcessor implements ItemProcessor<SubscriptionContext, InvoiceEntity> {

    private final PlanMealPriceContext planMealPriceContext;
    private final InvoiceDataAccessMapper invoiceDataAccessMapper;

    @Autowired
    public InvoiceItemProcessor(PlanMealPriceContext planMealPriceContext, InvoiceDataAccessMapper invoiceDataAccessMapper) {
        this.planMealPriceContext = planMealPriceContext;
        this.invoiceDataAccessMapper = invoiceDataAccessMapper;
    }

    @Override
    public @Nullable InvoiceEntity process(SubscriptionContext subscription) throws Exception {
        log.info("Processing subscription: {}", subscription);
        Invoice processedInvoice = Invoice.builder()
                               .subscriptionId(new SubscriptionId(subscription.getSubscriptionId()))
                               .userId(new UserId(subscription.getUserId()))
                               .providedUserId(new UserId(subscription.getProvidedUserId()))
                               .amount(this.calculateBillingAmount(subscription.getPlanMealIds(),
                                                                   planMealPriceContext.getPriceMap()))
                               .build();

        LocalDate today = LocalDate.now();
        LocalDate periodStart = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate periodEnd = today.with(TemporalAdjusters.lastDayOfMonth());

        processedInvoice.initializeInvoice(periodStart, periodEnd);
        log.info("Invoice created: {}", processedInvoice);
        return invoiceDataAccessMapper.invoiceToInvoiceEntity(processedInvoice);
    }

    private Money calculateBillingAmount(List<UUID> planMealIds, Map<UUID, BigDecimal> planMealPricesMap) {
        return planMealIds.stream()
                          .map(planMealPricesMap::get)
                          .filter(Objects::nonNull)
                          .map(Money::new)
                          .reduce(new Money(BigDecimal.ZERO), Money::add);
    }
}
