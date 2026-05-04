package com.trustamarket.walletservice.wallet.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ChargeCompleteRequest(
        @NotNull UUID userId,
        @NotNull UUID paymentId,
        @Positive long chargeAmount
) {
}
