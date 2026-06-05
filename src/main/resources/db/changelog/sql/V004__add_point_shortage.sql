--liquibase formatted sql

--changeset ihyein:1
ALTER TABLE p_point_transaction_request_history
    ADD COLUMN IF NOT EXISTS ref_id UUID;

CREATE TABLE IF NOT EXISTS p_point_shortage (
    id                                    UUID        PRIMARY KEY,
    point_transaction_request_history_id  UUID        NOT NULL UNIQUE,
    shortage                              BIGINT      NOT NULL,
    balance                               BIGINT      NOT NULL,
    CONSTRAINT fk_shortage_request_history
        FOREIGN KEY (point_transaction_request_history_id)
        REFERENCES p_point_transaction_request_history(point_transaction_request_history_id)
);