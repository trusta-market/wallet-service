package com.trustamarket.walletservice.wallet.application.dto.command;

import com.trustamarket.walletservice.wallet.domain.enums.PointRequestStatus;

import java.util.UUID;

public record ChargeCompleteCommand (
        UUID userId,
        UUID paymentId,
        UUID pointTxRequestHistoryId,
        PointRequestStatus requestResultStatus,
        long chargedAmount
) {
    public ChargeCompleteCommand {
        if (userId == null) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
        if (paymentId == null) {
            throw new IllegalArgumentException("paymentId는 필수입니다.");
        }
        if (chargedAmount <= 0) {
            throw new IllegalArgumentException("충전 금액은 1원 이상이어야 합니다.");
        }
    }
}