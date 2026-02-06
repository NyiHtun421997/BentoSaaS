package com.nyihtuun.bentosystem.invoiceservice.application_service.batch;

import com.nyihtuun.bentosystem.invoiceservice.InvoiceConstants;
import com.nyihtuun.bentosystem.invoiceservice.application_service.impl.grpc.PlanManagementServiceGrpcClient;
import com.nyihtuun.bentosystem.invoiceservice.application_service.impl.grpc.SubscriptionServiceGrpcClient;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import plan_management_grpc.PlanManagementResponse;
import subscription_grpc.SubscriptionResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component(InvoiceConstants.INVOICE_ITEM_READER)
@StepScope
public class InvoiceItemReader implements ItemReader<SubscriptionContext>, StepExecutionListener {

    private final PlanManagementServiceGrpcClient planManagementServiceGrpcClient;
    private final SubscriptionServiceGrpcClient subscriptionServiceGrpcClient;
    private final PlanMealPriceContext planMealPriceContext;

    private Iterator<SubscriptionContext> subscriptionContextIterator;

    @Autowired
    public InvoiceItemReader(PlanManagementServiceGrpcClient planManagementServiceGrpcClient,
                             SubscriptionServiceGrpcClient subscriptionServiceGrpcClient, PlanMealPriceContext planMealPriceContext) {
        this.planManagementServiceGrpcClient = planManagementServiceGrpcClient;
        this.subscriptionServiceGrpcClient = subscriptionServiceGrpcClient;
        this.planMealPriceContext = planMealPriceContext;
    }

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {
        log.info("Fetching active subscriptions for invoice generation.");
        SubscriptionResponse subscriptionResponse = subscriptionServiceGrpcClient.fetchActiveSubscriptions(Instant.now());

        Set<String> planIds = subscriptionResponse.getSubscriptionsList()
                                                  .stream()
                                                  .map(SubscriptionResponse.Subscription::getPlanId)
                                                  .collect(Collectors.toSet());

        subscriptionContextIterator = subscriptionResponse.getSubscriptionsList()
                                                          .stream()
                                                          .map(getSubscriptionSubscriptionContextFunction())
                                                          .iterator();

        log.info("Fetching plan meal prices for invoice generation.");
        PlanManagementResponse planManagementResponse = planManagementServiceGrpcClient.fetchPlanMealPrices(planIds.stream().toList());
        Map<UUID, BigDecimal> planMealPrices = planManagementResponse.getPlanDetailsList()
                                                                     .stream()
                                                                     .flatMap(planDetail -> planDetail.getPlanMealsList().stream())
                                                                     .collect(Collectors.toMap(planMeal -> UUID.fromString(planMeal.getPlanMealId()),
                                                                                               planMeal -> new BigDecimal(planMeal.getPricePerMonth())));

        planMealPriceContext.setPriceMap(planMealPrices);
    }

    private @NonNull Function<SubscriptionResponse.Subscription, SubscriptionContext> getSubscriptionSubscriptionContextFunction() {
        return subscription ->
                SubscriptionContext.builder()
                                   .subscriptionId(UUID.fromString(
                                           subscription.getSubscriptionId()))
                                   .planId(UUID.fromString(
                                           subscription.getPlanId()))
                                   .userId(UUID.fromString(
                                           subscription.getUserId()))
                                   .providedUserId(UUID.fromString(
                                           subscription.getProvidedUserId()))
                                   .planMealIds(subscription.getPlanMealIdsList()
                                                            .stream()
                                                            .map(UUID::fromString)
                                                            .toList())
                                   .build();
    }

    @Override
    public @Nullable SubscriptionContext read() throws Exception {
        if (subscriptionContextIterator == null || !subscriptionContextIterator.hasNext()) return null;
        return subscriptionContextIterator.next();
    }
}
