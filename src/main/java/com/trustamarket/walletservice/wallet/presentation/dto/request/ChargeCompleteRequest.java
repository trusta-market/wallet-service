package com.trustamarket.walletservice.wallet.presentation.dto.request;

import com.trustamarket.walletservice.wallet.domain.enums.PointRequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ChargeCompleteRequest(
        @NotNull UUID userId,
        @NotNull UUID paymentId,
        @NotNull UUID pointTxRequestHistoryId,
        @NotNull PointRequestStatus paymentStatus,
        @Positive long chargeAmount
) {
}
