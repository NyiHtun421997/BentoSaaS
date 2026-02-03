-- ==========================================================
-- init-schema.sql for USER-INFO / AUTH BC
-- Schema: "userinfo"
-- Tables: role, address, "user", user_role
-- ==========================================================

DROP SCHEMA IF EXISTS "userinfo" CASCADE;

CREATE SCHEMA "userinfo";

-- UUID generator
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================================================
-- role
-- ==========================================================
DROP TABLE IF EXISTS "userinfo".role;
CREATE TABLE "userinfo".role
(
    id   BIGINT  NOT NULL,
    name VARCHAR NOT NULL,
    CONSTRAINT role_pk PRIMARY KEY (id),
    CONSTRAINT role_unique_name UNIQUE (name)
);

-- ==========================================================
-- address
-- ==========================================================
DROP TABLE IF EXISTS "userinfo".address;
CREATE TABLE "userinfo".address
(
    id                    uuid                 NOT NULL,
    building_name_room_no VARCHAR              NOT NULL,
    chome_ban_go          VARCHAR              NOT NULL,
    district              VARCHAR              NOT NULL,
    postal_code           VARCHAR              NOT NULL,
    city                  VARCHAR              NOT NULL,
    prefecture            VARCHAR              NOT NULL,

    CONSTRAINT address_pk PRIMARY KEY (id)
);

-- ==========================================================
-- user
-- ==========================================================
DROP TABLE IF EXISTS "userinfo"."user";
CREATE TABLE "userinfo"."user"
(
    user_id            uuid      NOT NULL,
    email              VARCHAR   NOT NULL,
    encrypted_password VARCHAR   NOT NULL,
    first_name         VARCHAR   NOT NULL,
    last_name          VARCHAR   NOT NULL,
    address_id         uuid,
    ph_no              VARCHAR   NOT NULL,
    created_at         TIMESTAMP NOT NULL,
    updated_at         TIMESTAMP NOT NULL,
    description        VARCHAR,

    CONSTRAINT user_pk PRIMARY KEY (user_id),
    CONSTRAINT user_unique_email UNIQUE (email),

    CONSTRAINT user_address_fk FOREIGN KEY (address_id)
        REFERENCES "userinfo".address (id)
        ON UPDATE NO ACTION
        ON DELETE SET NULL
);

-- ==========================================================
-- Many-to-many
-- ==========================================================
DROP TABLE IF EXISTS "userinfo".user_role;
CREATE TABLE "userinfo".user_role
(
    user_id uuid   NOT NULL,
    role_id BIGINT NOT NULL,

    CONSTRAINT user_role_pk PRIMARY KEY (user_id, role_id),

    CONSTRAINT user_role_user_fk FOREIGN KEY (user_id)
        REFERENCES "userinfo"."user" (user_id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE,

    CONSTRAINT user_role_role_fk FOREIGN KEY (role_id)
        REFERENCES "userinfo".role (id)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT
);