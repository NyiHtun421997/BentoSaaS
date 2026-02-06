package com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.message;

import java.util.List;
import java.util.UUID;

public interface UserPlanSubscriptionMessageListener {
    void reflectUserPlanSubscription(UUID planId, List<UUID> appliedPlanMealIds, List<UUID> unappliedPlanMealIds);
}
