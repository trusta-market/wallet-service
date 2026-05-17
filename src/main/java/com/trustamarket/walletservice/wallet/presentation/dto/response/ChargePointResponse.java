package com.trustamarket.walletservice.wallet.presentation.dto.response;

import com.trustamarket.walletservice.wallet.application.dto.result.ChargePointResult;

import java.time.Instant;
import java.util.UUID;

public record ChargePointResponse (
        UUID paymentId,
        UUID pointTxRequestHistoryId,
        long requestAmount
) {
    public static ChargePointResponse from(ChargePointResult result) {
        return new ChargePointResponse(
                result.paymentId(),
                result.pointTxRequestHistoryId(),
                result.requestAmount()
        );
    }
}
