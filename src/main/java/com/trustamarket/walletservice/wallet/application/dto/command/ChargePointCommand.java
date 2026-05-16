package com.trustamarket.walletservice.wallet.application.dto.command;

import java.util.UUID;

public record ChargePointCommand (
    UUID userId,
    String idempotencyKey,
    long chargeAmount
) {
    public ChargePointCommand {
        if (userId == null) {
            throw new IllegalArgumentException("userId 값은 필수입니다.");
        }
        if (chargeAmount <= 0) {
            throw new IllegalArgumentException("충전 금액은 1원 이상이어야 합니다.");
        }
    }

    public static ChargePointCommand of(UUID userId, String idempotencyKey, long chargeAmount) {
        return new ChargePointCommand(userId, idempotencyKey, chargeAmount);
    }
}
