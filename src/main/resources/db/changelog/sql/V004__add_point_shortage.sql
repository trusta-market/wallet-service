--liquibase formatted sql

--changeset ihyein:1
ALTER TABLE p_point_transaction_request_history
    ADD COLUMN IF NOT EXISTS ref_id UUID;

--changeset ihyein:2
ALTER TABLE p_point_transaction_request_history
DROP CONSTRAINT uk_idempotency_key,
    ADD CONSTRAINT uk_idempotency_key_ref_id_request_type
        UNIQUE (idempotency_key, ref_id, request_type);

--changeset ihyein:3
CREATE TABLE IF NOT EXISTS p_point_shortage (
    id                                    UUID        PRIMARY KEY,
    point_transaction_request_history_id  UUID        NOT NULL UNIQUE,
    shortage                              BIGINT      NOT NULL,
    balance                               BIGINT      NOT NULL,
    CONSTRAINT fk_shortage_request_history
        FOREIGN KEY (point_transaction_request_history_id)
        REFERENCES p_point_transaction_request_history(point_transaction_request_history_id)
);