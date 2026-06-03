--liquibase formatted sql

--changeset ihyein:1
CREATE TABLE IF NOT EXISTS p_system_wallet_outbox (
    outbox_id UUID NOT NULL,
    wallet_id UUID NOT NULL,
    delta_amount BIGINT NOT NULL,
    tx_type VARCHAR(50) NOT NULL,
    ref_id UUID NOT NULL,
    ref_type VARCHAR(20) NOT NULL,
    outbox_status VARCHAR(20) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at TIMESTAMPTZ,
    CONSTRAINT pk_system_wallet_outbox PRIMARY KEY (outbox_id),
    CONSTRAINT uk_sw_outbox_ref_id_tx_type UNIQUE (ref_id, tx_type)
);
