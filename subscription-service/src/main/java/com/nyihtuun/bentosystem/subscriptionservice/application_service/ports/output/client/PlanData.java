package com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client;

import java.util.List;
import java.util.UUID;

public record PlanData(UUID planId, List<UUID> planMealIds) {
}
