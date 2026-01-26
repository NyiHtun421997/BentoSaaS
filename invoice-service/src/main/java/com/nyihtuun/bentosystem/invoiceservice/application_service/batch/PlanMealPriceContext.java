package com.nyihtuun.bentosystem.invoiceservice.application_service.batch;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
@StepScope
public class PlanMealPriceContext {
    private Map<UUID, BigDecimal> priceMap;
    public Map<UUID, BigDecimal> getPriceMap() { return priceMap; }
    public void setPriceMap(Map<UUID, BigDecimal> priceMap) { this.priceMap = priceMap; }
}
