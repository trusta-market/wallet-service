--liquibase formatted sql

--changeset seungwon:1
CREATE TABLE IF NOT EXISTS p_wallets (
    wallet_id   UUID         NOT NULL,
    point       BIGINT,
    status      VARCHAR(255),
    user_id     UUID,
    wallet_type VARCHAR(255),
    CONSTRAINT pk_p_wallets      PRIMARY KEY (wallet_id),
    CONSTRAINT uk_wallet_user_id UNIQUE (user_id)
);

--changeset seungwon:2
CREATE TABLE IF NOT EXISTS p_point_transactions (
    point_transaction_id UUID         NOT NULL,
    created_at           TIMESTAMPTZ,
    amount               BIGINT,
    balance_after        BIGINT,
    tx_type              VARCHAR(255) NOT NULL,
    ref_id               UUID,
    ref_type             VARCHAR(255),
    wallet_id            UUID         NOT NULL,
    CONSTRAINT pk_p_point_transactions PRIMARY KEY (point_transaction_id),
    CONSTRAINT uk_point_tx_ref_type    UNIQUE (ref_id, tx_type),
    CONSTRAINT fk_point_tx_wallet      FOREIGN KEY (wallet_id) REFERENCES p_wallets (wallet_id)
);

--changeset seungwon:3
CREATE TABLE IF NOT EXISTS p_point_transaction_request_history (
    point_transaction_request_history_id UUID         NOT NULL,
    request_point                        BIGINT       NOT NULL,
    request_type                         VARCHAR(255),
    status                               VARCHAR(255),
    wallet_id                            UUID         NOT NULL,
    created_at                           TIMESTAMPTZ,
    updated_at                           TIMESTAMPTZ,
    idempotency_key                      VARCHAR(255),
    CONSTRAINT pk_p_point_tx_req_history PRIMARY KEY (point_transaction_request_history_id),
    CONSTRAINT uk_idempotency_key        UNIQUE (idempotency_key),
    CONSTRAINT fk_point_tx_req_wallet    FOREIGN KEY (wallet_id) REFERENCES p_wallets (wallet_id)
);

--changeset seungwon:4
CREATE TABLE IF NOT EXISTS p_settlement_history (
    settlement_history_id UUID         NOT NULL,
    applied_fee_rate      NUMERIC(5,4),
    event_id              UUID         NOT NULL,
    order_id              UUID         NOT NULL,
    processed_at          TIMESTAMPTZ  NOT NULL,
    seller_id             UUID         NOT NULL,
    total_amount          BIGINT,
    seller_amount         BIGINT,
    fee_revenue_amount    BIGINT,
    status                VARCHAR(255) NOT NULL,
    CONSTRAINT pk_p_settlement_history        PRIMARY KEY (settlement_history_id),
    CONSTRAINT uk_settlement_history_event_id UNIQUE (event_id)
);
