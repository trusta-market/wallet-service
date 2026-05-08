package com.trustamarket.walletservice.wallet.presentation.dto.response;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.application.dto.result.WithdrawPointResult;

public record WithdrawPointResponse(
    UUID pointTxRequestHistoryId
) {
    public static WithdrawPointResponse from(WithdrawPointResult result) {
        return new WithdrawPointResponse(result.pointTxRequestHistoryId());
    }
}
