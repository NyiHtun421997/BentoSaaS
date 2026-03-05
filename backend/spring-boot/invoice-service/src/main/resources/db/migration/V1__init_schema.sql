-- DROP SCHEMA IF EXISTS "invoice" CASCADE;

CREATE SCHEMA IF NOT EXISTS "invoice";

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'invoice_status') THEN
        CREATE TYPE invoice_status AS ENUM ('ISSUED', 'PAID', 'CANCELLED', 'FAILED');
    END IF;
END $$;

-- DROP TABLE IF EXISTS "invoice".invoice;
CREATE TABLE IF NOT EXISTS "invoice".invoice
(
    id                  uuid           NOT NULL,
    subscription_id     uuid           NOT NULL,
    user_id             uuid           NOT NULL,
    provided_user_id    uuid           NOT NULL,
    invoice_status      invoice_status NOT NULL,
    amount              NUMERIC(10, 2) NOT NULL CHECK ( amount > 0 ),
    subscribed_meal_ids jsonb          NOT NULL DEFAULT '[]',
    issued_at           TIMESTAMPTZ    NOT NULL,
    updated_at          TIMESTAMPTZ,
    paid_at             TIMESTAMPTZ,
    period_start        DATE           NOT NULL,
    period_end          DATE           NOT NULL,

    CONSTRAINT invoice_pk PRIMARY KEY (id),

    -- Prevent duplicate invoices for the same subscription and billing period
    CONSTRAINT invoice_unique_subscription_period UNIQUE (subscription_id, period_start, period_end),

    -- Period sanity check
    CONSTRAINT invoice_period_valid CHECK ( period_end >= period_start )
);

-- ==========================================================
-- Invoice BC: Outbox Table
-- ==========================================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'outbox_status') THEN
        CREATE TYPE outbox_status AS ENUM ('STARTED', 'COMPLETED', 'FAILED');
    END IF;
END $$;

-- DROP TABLE IF EXISTS "invoice".invoice_event_outbox CASCADE;;
CREATE TABLE IF NOT EXISTS "invoice".invoice_event_outbox
(
    id            uuid          NOT NULL,
    user_id       uuid          NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL,
    processed_at  TIMESTAMPTZ,
    payload       jsonb         NOT NULL,
    outbox_status outbox_status NOT NULL,
    version       integer       NOT NULL,
    topic_name    varchar(255)  NOT NULL,
    type          varchar(255)  NOT NULL,
    CONSTRAINT invoice_event_outbox_pkey PRIMARY KEY (id),
    CONSTRAINT type_allowed CHECK (type IN ('NOTIFICATION'))
);

CREATE TABLE IF NOT EXISTS "invoice".payment
(
    id              varchar(255)   NOT NULL PRIMARY KEY,
    invoice_id      uuid           NOT NULL,
    idempotency_key uuid UNIQUE    NOT NULL,
    payment_status  varchar(255)   NOT NULL,
    created_at      TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ,
    amount          NUMERIC(10, 2) NOT NULL CHECK ( amount > 0 ),
    currency        varchar(3)     NOT NULL,
    payment_ref     varchar(255),
    CONSTRAINT status_allowed CHECK (payment_status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    CONSTRAINT invoice_id_payment_status_unique UNIQUE (invoice_id, payment_status)
);