package com.trustamarket.walletservice.wallet.infrastructure.payment.dto;

import java.util.UUID;

public record WithdrawRequest(
        UUID userId,
        UUID pointTxRequestHistoryId,
        long withdrawAmount
) {
    public WithdrawRequest {
        if (userId == null) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
        if (pointTxRequestHistoryId == null) {
            throw new IllegalArgumentException("pointTxRequestHitoryId는 필수입니다.");
        }
        if (withdrawAmount <= 0) {
            throw new IllegalArgumentException("withdrawAmount 필수입니다.");
        }
    }
}
