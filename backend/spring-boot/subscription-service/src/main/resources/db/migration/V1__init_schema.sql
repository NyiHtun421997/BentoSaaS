-- DROP SCHEMA IF EXISTS "subscription" CASCADE;;

CREATE SCHEMA IF NOT EXISTS "subscription";;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";;

-- DROP TYPE IF EXISTS subscription_status;;
CREATE TYPE subscription_status AS ENUM ('APPLIED', 'SUBSCRIBED', 'CANCELLED', 'SUSPENDED');

-- DROP TABLE IF EXISTS "subscription".subscription;;
CREATE TABLE IF NOT EXISTS "subscription".subscription
(
    id                  uuid                NOT NULL,
    user_id             uuid                NOT NULL,
    plan_id             uuid                NOT NULL,
    provided_user_id    uuid                NOT NULL,
    subscription_status subscription_status NOT NULL,
    applied_at          TIMESTAMPTZ         NOT NULL,
    updated_at          TIMESTAMPTZ         NOT NULL,
    cancelled_at        TIMESTAMPTZ,
    activated_at        TIMESTAMPTZ,
    CONSTRAINT subscription_pk PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS subscription_unique_active_user_plan
    ON "subscription".subscription (user_id, plan_id)
    WHERE (cancelled_at IS NULL);

-- DROP TABLE IF EXISTS "subscription".meal_selection;;
CREATE TABLE IF NOT EXISTS "subscription".meal_selection
(
    subscription_id uuid NOT NULL,
    plan_meal_id    uuid NOT NULL,
    CONSTRAINT meal_selection_pk PRIMARY KEY (subscription_id, plan_meal_id),
    CONSTRAINT meal_selection_subscription_id_fk FOREIGN KEY (subscription_id)
        REFERENCES "subscription".subscription (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

-- ==========================================================
-- Subscription BC: Outbox Table
-- ==========================================================

-- DROP TYPE IF EXISTS outbox_status;;
CREATE TYPE outbox_status AS ENUM ('STARTED', 'COMPLETED', 'FAILED');

-- DROP TABLE IF EXISTS "subscription".user_plan_subscription_event_outbox CASCADE;;
CREATE TABLE IF NOT EXISTS "subscription".user_plan_subscription_event_outbox
(
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    processed_at TIMESTAMPTZ,
    payload jsonb NOT NULL,
    outbox_status outbox_status NOT NULL,
    version integer NOT NULL,
    topic_name varchar(255) NOT NULL,
    type varchar(255) NOT NULL,
    CONSTRAINT user_plan_subscription_event_outbox_pkey PRIMARY KEY (id),
    CONSTRAINT type_allowed CHECK (type IN ('DATA_CHANGED', 'NOTIFICATION'))
);