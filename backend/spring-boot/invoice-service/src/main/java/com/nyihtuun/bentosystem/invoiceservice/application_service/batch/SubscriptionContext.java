package com.nyihtuun.bentosystem.invoiceservice.application_service.batch;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SubscriptionContext implements Serializable {
    private UUID subscriptionId;
    private UUID planId;
    private UUID userId;
    private UUID providedUserId;
    private List<UUID> planMealIds;
}
