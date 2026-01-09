package com.nyihtuun.bentosystem.domain.valueobject;

import java.util.UUID;

public class InvoiceId extends BaseId<UUID> {
    protected InvoiceId(UUID value) {
        super(value);
    }
}
