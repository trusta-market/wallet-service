package com.trustamarket.walletservice.wallet.application.dto.command;

import java.util.UUID;

public record WithdrawPointCommand(
    UUID userId,
    long withdrawAmount
) {
    public WithdrawPointCommand {
        if (userId == null) {
            throw new IllegalArgumentException("userId 값은 필수입니다.");
        }
        if (withdrawAmount <= 0) {
            throw new IllegalArgumentException("출금 금액은 1원 이상이어야 합니다.");
        }
    }

    public static WithdrawPointCommand of(UUID userId, long withdrawAmount) {
        return new WithdrawPointCommand(userId, withdrawAmount);
    }
}
