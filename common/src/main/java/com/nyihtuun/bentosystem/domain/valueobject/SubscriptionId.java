package com.nyihtuun.bentosystem.domain.valueobject;

import java.util.UUID;

public class SubscriptionId extends BaseId<UUID> {
    protected SubscriptionId(UUID value) {
        super(value);
    }
}
