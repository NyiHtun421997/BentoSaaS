DROP SCHEMA IF EXISTS "planmanagement" CASCADE;

CREATE SCHEMA "planmanagement";

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS postgis;

DROP TYPE IF EXISTS plan_status;
CREATE TYPE plan_status AS ENUM ('RECRUITING', 'ACTIVE', 'SUSPENDED', 'CANCELLED', 'PLANS_ADDED', 'PLANS_REMOVED');

DROP TABLE IF EXISTS "planmanagement".address CASCADE;

CREATE TABLE "planmanagement".address
(
    id                    uuid    NOT NULL DEFAULT gen_random_uuid(),
    building_name_room_no VARCHAR NOT NULL,
    chome_ban_go          VARCHAR NOT NULL,
    district              VARCHAR NOT NULL,
    postal_code           VARCHAR NOT NULL,
    city                  VARCHAR NOT NULL,
    prefecture            VARCHAR NOT NULL,
    location              geography(Point, 4326),
    CONSTRAINT address_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_address_location
    ON "planmanagement".address
        USING GIST (location);

DROP TABLE IF EXISTS "planmanagement".category CASCADE;

DROP TABLE IF EXISTS "planmanagement".plan CASCADE;

CREATE TABLE "planmanagement".plan
(
    id                       uuid           NOT NULL,
    code                     CHAR(7)        NOT NULL UNIQUE,
    title                    VARCHAR        NOT NULL,
    description              VARCHAR,
    plan_status              plan_status    NOT NULL,
    created_at               TIMESTAMP      NOT NULL,
    updated_at               TIMESTAMP      NOT NULL,
    user_id                  uuid           NOT NULL,
    skip_dates                jsonb          NOT NULL DEFAULT '[]',
    address_id               uuid           NOT NULL,
    display_subscription_fee NUMERIC(10, 2) NOT NULL CHECK ( display_subscription_fee > 0 ),
    delete_flag              BOOLEAN                 DEFAULT FALSE,
    deleted_at               TIMESTAMP,
    CONSTRAINT plan_pk PRIMARY KEY (id),
    CONSTRAINT plan_title_description_unique UNIQUE (title, description),
    CONSTRAINT plan_address_fk FOREIGN KEY (address_id)
        REFERENCES "planmanagement".address (id)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT,
    CONSTRAINT plan_skipdates_max2days CHECK ( jsonb_array_length(skip_dates) <= 2 )
);

DROP TABLE IF EXISTS "planmanagement".plan_meal CASCADE;

CREATE TABLE "planmanagement".plan_meal
(
    id                uuid           NOT NULL,
    plan_id           uuid           NOT NULL,
    name              VARCHAR        NOT NULL,
    description       VARCHAR,
    price_per_month   NUMERIC(10, 2) NOT NULL,
    is_primary        BOOLEAN DEFAULT FALSE,
    min_sub_count     INT     DEFAULT 1 CHECK ( min_sub_count > 0 ),
    current_sub_count INT     DEFAULT 0 CHECK ( current_sub_count >= 0 ),
    image_url         VARCHAR,
    created_at        TIMESTAMP      NOT NULL,
    updated_at        TIMESTAMP      NOT NULL,
    delete_flag       BOOLEAN DEFAULT FALSE,
    deleted_at        TIMESTAMP,
    CONSTRAINT plan_meal_pkey PRIMARY KEY (id),
    CONSTRAINT plan_meal_plan_id_fk FOREIGN KEY (plan_id)
        REFERENCES "planmanagement".plan (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

CREATE TABLE "planmanagement".category
(
    id   uuid    NOT NULL,
    name VARCHAR NOT NULL UNIQUE,
    CONSTRAINT category_pkey PRIMARY KEY (id)
);

DROP TABLE IF EXISTS "planmanagement".plan_category CASCADE;

CREATE TABLE "planmanagement".plan_category
(
    plan_id     uuid NOT NULL,
    category_id uuid NOT NULL,
    CONSTRAINT plan_category_plan_id_fk FOREIGN KEY (plan_id)
        REFERENCES "planmanagement".plan (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT plan_category_category_id_fk FOREIGN KEY (category_id)
        REFERENCES "planmanagement".category (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT plan_category_pk PRIMARY KEY (plan_id, category_id)
);

DROP TABLE IF EXISTS "planmanagement".delivery_schedule CASCADE;

CREATE TABLE "planmanagement".delivery_schedule
(
    id           uuid      NOT NULL,
    plan_id      uuid      NOT NULL,
    period_start DATE      NOT NULL,
    period_end   DATE      NOT NULL,
    created_at   TIMESTAMP NOT NULL,
    CONSTRAINT delivery_schedule_pk PRIMARY KEY (id),
    CONSTRAINT delivery_schedule_unique UNIQUE (plan_id, period_start, period_end),
    CONSTRAINT delivery_schedule_plan_id_fk FOREIGN KEY (plan_id)
        REFERENCES "planmanagement".plan (id)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT
);

DROP TABLE IF EXISTS "planmanagement".delivery_schedule_detail CASCADE;

CREATE TABLE "planmanagement".delivery_schedule_detail
(
    id                   uuid NOT NULL,
    delivery_schedule_id uuid NOT NULL,
    plan_meal_id         uuid NOT NULL,
    delivery_date        DATE NOT NULL,
    CONSTRAINT delivery_schedule_detail_pk PRIMARY KEY (id),
    CONSTRAINT delivery_schedule_detail_delivery_schedule_id_fk FOREIGN KEY (delivery_schedule_id)
        REFERENCES "planmanagement".delivery_schedule (id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT delivery_schedule_detail_plan_meal_id_fk FOREIGN KEY (plan_meal_id)
        REFERENCES "planmanagement".plan_meal (id)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT
);

-- ==========================================================
-- Plan Management BC: Cron job execution results (job_run)
-- ==========================================================
DROP TYPE IF EXISTS job_run_status;
CREATE TYPE job_run_status AS ENUM ('SUCCESS', 'PARTIAL_SUCCESS', 'FAILED');

DROP TABLE IF EXISTS "planmanagement".job_run CASCADE;
CREATE TABLE "planmanagement".job_run
(
    id            uuid PRIMARY KEY,

    job_type      VARCHAR(100) NOT NULL,

    period_start  DATE         NOT NULL,
    period_end    DATE         NOT NULL,

    status        job_run_status  NOT NULL, -- SUCCESS / PARTIAL_SUCCESS / FAILED

    started_at    TIMESTAMP  NOT NULL,
    finished_at   TIMESTAMP,

    total_targets INT          NOT NULL DEFAULT 0,
    success_count INT          NOT NULL DEFAULT 0,
    failure_count INT          NOT NULL DEFAULT 0,

    results       jsonb        NOT NULL DEFAULT '[]'::jsonb,
    error         jsonb,

    created_at    TIMESTAMP  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_pm_job_run_job_period
    on "planmanagement".job_run (job_type, period_start, period_end);

CREATE INDEX IF NOT EXISTS ix_pm_job_run_job_type_started_at
    on "planmanagement".job_run (job_type, started_at desc);

CREATE INDEX IF NOT EXISTS  ix_pm_job_run_job_type_status
    on "planmanagement".job_run (job_type, status);