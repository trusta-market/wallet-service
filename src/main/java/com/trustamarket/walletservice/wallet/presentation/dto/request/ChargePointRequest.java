package com.trustamarket.walletservice.wallet.presentation.dto.request;

import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ChargePointRequest(
        @Positive long chargeAmount
) {}
