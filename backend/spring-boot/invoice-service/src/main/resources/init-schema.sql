DROP SCHEMA IF EXISTS "invoice" CASCADE;

CREATE SCHEMA "invoice";

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TYPE IF EXISTS invoice_status;
CREATE TYPE invoice_status AS ENUM ('ISSUED', 'PAID', 'CANCELLED', 'FAILED');

DROP TABLE IF EXISTS "invoice".invoice;
CREATE TABLE "invoice".invoice
(
    id             uuid           NOT NULL,
    subscription_id uuid          NOT NULL,
    user_id        uuid           NOT NULL,
    provided_user_id uuid         NOT NULL,
    invoice_status invoice_status NOT NULL,
    amount         NUMERIC(10, 2) NOT NULL CHECK ( amount > 0 ),
    issued_at      TIMESTAMPTZ    NOT NULL,
    updated_at     TIMESTAMPTZ,
    paid_at        TIMESTAMPTZ,
    period_start   DATE           NOT NULL,
    period_end     DATE           NOT NULL,

    CONSTRAINT invoice_pk PRIMARY KEY (id),

    -- Prevent duplicate invoices for the same subscription and billing period
    CONSTRAINT invoice_unique_subscription_period UNIQUE (subscription_id, period_start, period_end),

    -- Period sanity check
    CONSTRAINT invoice_period_valid CHECK ( period_end >= period_start ),

    -- paid_at should exist only when status is PAID (optional but useful)
    CONSTRAINT invoice_paid_at_consistency CHECK (
        (invoice_status = 'PAID' AND paid_at IS NOT NULL)
            OR (invoice_status <> 'PAID' AND paid_at IS NULL)
        )
);