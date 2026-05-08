package com.trustamarket.walletservice.wallet.application.dto.command;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.enums.PointRequestStatus;

public record WithdrawCompleteCommand(
        UUID userId,
        UUID paymentId,
        UUID pointTxRequestHistoryId,
        PointRequestStatus requestResultStatus,
        long withdrawAmount
) {
    public WithdrawCompleteCommand {
        if (userId == null) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
        if (pointTxRequestHistoryId == null) {
            throw new IllegalArgumentException("pointTxRequestHistoryId 필수입니다.");
        }
        if (withdrawAmount <= 0) {
            throw new IllegalArgumentException("출금 금액은 1원 이상이어야 합니다.");
        }
    }

    public static WithdrawCompleteCommand of(UUID userId, UUID paymentId, UUID pointTxRequestHistoryId,
        PointRequestStatus requestResultStatus, long withdrawAmount) {
        return new WithdrawCompleteCommand(userId, paymentId, pointTxRequestHistoryId, requestResultStatus, withdrawAmount);
    }
}