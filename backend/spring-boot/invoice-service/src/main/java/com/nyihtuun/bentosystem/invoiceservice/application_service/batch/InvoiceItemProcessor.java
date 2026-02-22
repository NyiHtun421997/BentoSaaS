package com.nyihtuun.bentosystem.invoiceservice.application_service.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Message;
import com.nyihtuun.bentosystem.domain.valueobject.*;
import com.nyihtuun.bentosystem.domain.valueobject.status.OutboxStatus;
import com.nyihtuun.bentosystem.invoiceservice.InvoiceConstants;
import com.nyihtuun.bentosystem.invoiceservice.application_service.outbox.model.InvoiceEventOutboxMessage;
import com.nyihtuun.bentosystem.invoiceservice.application_service.ports.output.repository.InvoiceEventOutboxRepository;
import com.nyihtuun.bentosystem.invoiceservice.configuration.InvoiceConfigData;
import com.nyihtuun.bentosystem.invoiceservice.data_access.jpa_entity.InvoiceEntity;
import com.nyihtuun.bentosystem.invoiceservice.data_access.mapper.InvoiceDataAccessMapper;
import com.nyihtuun.bentosystem.invoiceservice.domain.entity.Invoice;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Supplier;

import static com.nyihtuun.bentosystem.domain.utility.CommonConstants.INVOICE_ISSUED_NOTIFICATION_EVENT;
import static com.nyihtuun.bentosystem.domain.utility.CommonConstants.NOTIFICATION;
import static com.nyihtuun.bentosystem.invoiceservice.application_service.impl.service.InvoiceEventHelper.INVOICE_NOTIFICATION_EVENT_CREATION_FUNCTION;

@Slf4j
@Component(InvoiceConstants.INVOICE_ITEM_PROCESSOR)
@StepScope
public class InvoiceItemProcessor implements ItemProcessor<SubscriptionContext, InvoiceEntity> {

    private final PlanMealPriceContext planMealPriceContext;
    private final InvoiceDataAccessMapper invoiceDataAccessMapper;
    private final InvoiceEventOutboxRepository invoiceEventOutboxRepository;
    private final InvoiceConfigData invoiceConfigData;
    private final ObjectMapper objectMapper;

    @Autowired
    public InvoiceItemProcessor(PlanMealPriceContext planMealPriceContext,
                                InvoiceDataAccessMapper invoiceDataAccessMapper,
                                InvoiceEventOutboxRepository invoiceEventOutboxRepository,
                                InvoiceConfigData invoiceConfigData,
                                ObjectMapper objectMapper) {
        this.planMealPriceContext = planMealPriceContext;
        this.invoiceDataAccessMapper = invoiceDataAccessMapper;
        this.invoiceEventOutboxRepository = invoiceEventOutboxRepository;
        this.invoiceConfigData = invoiceConfigData;
        this.objectMapper = objectMapper;
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
                                          .subscribedMealIds(subscription.getPlanMealIds()
                                                                         .stream()
                                                                         .map(PlanMealId::new)
                                                                         .toList())
                                          .build();

        LocalDate today = LocalDate.now();
        LocalDate periodStart = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate periodEnd = today.with(TemporalAdjusters.lastDayOfMonth());

        processedInvoice.initializeInvoice(periodStart, periodEnd);
        log.info("Invoice created: {}", processedInvoice);

        // Persist Notification Event in Outbox Table
        Supplier<Message> eventCreationSupplier = () -> INVOICE_NOTIFICATION_EVENT_CREATION_FUNCTION.execute(
                processedInvoice,
                subscription.getPlanId().toString(),
                objectMapper.valueToTree(processedInvoice)
        );

        createOutboxMessageAndPersist(processedInvoice,
                                      eventCreationSupplier);

        return invoiceDataAccessMapper.invoiceToInvoiceEntity(processedInvoice);
    }

    private void createOutboxMessageAndPersist(
            Invoice invoice,
            Supplier<Message> eventSupplier
    ) {
        log.info("Creating outbox event for invoice of subscription with id: {}.", invoice.getSubscriptionId().getValue().toString());
        InvoiceEventOutboxMessage outboxMessage = InvoiceEventOutboxMessage.builder()
                                                                           .id(UUID.randomUUID())
                                                                           .userId(invoice.getUserId().getValue())
                                                                           .topicName(invoiceConfigData.userNotificationTopicName())
                                                                           .type(NOTIFICATION)
                                                                           .outboxStatus(OutboxStatus.STARTED)
                                                                           .createdAt(Instant.now())
                                                                           .payload(eventSupplier.get())
                                                                           .build();
        invoiceEventOutboxRepository.save(outboxMessage);
        log.info("Outbox event: {} for invoice of subscription with id: {} was created.",
                 outboxMessage,
                 invoice.getSubscriptionId().getValue().toString());
    }

    private Money calculateBillingAmount(List<UUID> planMealIds, Map<UUID, BigDecimal> planMealPricesMap) {
        return planMealIds.stream()
                          .map(planMealPricesMap::get)
                          .filter(Objects::nonNull)
                          .map(Money::new)
                          .reduce(new Money(BigDecimal.ZERO), Money::add);
    }
}
