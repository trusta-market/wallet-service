package com.trustamarket.walletservice.wallet.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ChargedPointRequest (
    @NotNull UUID paymentId,
    @Positive long chargedAmount
) {}
