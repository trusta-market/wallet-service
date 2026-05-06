package com.trustamarket.walletservice.wallet.presentation.dto.request;

import jakarta.validation.constraints.Positive;

public record ChargePointRequest(
    @Positive long chargeAmount
) {}
