package com.trustamarket.walletservice.wallet.application.dto.command;

import java.util.UUID;

public record ChargePointCommand (
    UUID userId,
    UUID paymentId,
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
}
