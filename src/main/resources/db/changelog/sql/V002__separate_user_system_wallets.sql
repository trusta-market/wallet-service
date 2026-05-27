--liquibase formatted sql

--changeset ihyein:1
CREATE TABLE IF NOT EXISTS p_user_wallets (
    wallet_id  UUID         NOT NULL,
    point      BIGINT,
    status     VARCHAR(255),
    user_id    UUID         NOT NULL,
    version    BIGINT,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    CONSTRAINT pk_p_user_wallets       PRIMARY KEY (wallet_id),
    CONSTRAINT uk_user_wallet_user_id  UNIQUE (user_id)
);

--changeset ihyein:2
CREATE TABLE IF NOT EXISTS p_system_wallets (
    wallet_id          UUID         NOT NULL,
    point              BIGINT,
    status             VARCHAR(255),
    system_wallet_type VARCHAR(255),
    creator_id         UUID,
    created_at         TIMESTAMPTZ,
    updated_at         TIMESTAMPTZ,
    CONSTRAINT pk_p_system_wallets PRIMARY KEY (wallet_id)
);

--changeset ihyein:3
-- p_wallets의 유저 지갑 데이터 이전
-- version은 신규 컬럼이므로 0으로 초기화, 시각은 현재 시각으로 채움
INSERT INTO p_user_wallets (wallet_id, point, status, user_id, version, created_at, updated_at)
SELECT wallet_id, point, status, user_id, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM p_wallets
WHERE wallet_type = 'USER';

--changeset ihyein:4
-- p_wallets의 시스템 지갑 데이터 이전
-- wallet_type 값(SYSTEM_ESCROW, SYSTEM_FEE, SYSTEM_POINT_SOURCE)을 그대로 system_wallet_type으로 사용
-- creator_id는 기존 테이블에 없던 컬럼이므로 NULL로 이전
INSERT INTO p_system_wallets (wallet_id, point, status, system_wallet_type, creator_id, created_at, updated_at)
SELECT wallet_id, point, status, wallet_type, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM p_wallets
WHERE wallet_type != 'USER';

--changeset ihyein:5
-- p_point_transaction_request_history FK를 p_user_wallets로 교체
-- 요청 이력(충전·출금 요청)은 항상 유저 지갑에서만 발생하므로 참조 대상을 명확히 함
ALTER TABLE p_point_transaction_request_history
    DROP CONSTRAINT fk_point_tx_req_wallet,
    ADD CONSTRAINT fk_ptxreq_user_wallet FOREIGN KEY (wallet_id) REFERENCES p_user_wallets (wallet_id);

--changeset ihyein:6
-- p_point_transactions FK 제거
-- PointTransaction은 UserWallet·SystemWallet 둘 다 참조하므로 DB FK 대신 UUID로만 관리
ALTER TABLE p_point_transactions
    DROP CONSTRAINT fk_point_tx_wallet;

--changeset ihyein:7
DROP TABLE IF EXISTS p_wallets;
