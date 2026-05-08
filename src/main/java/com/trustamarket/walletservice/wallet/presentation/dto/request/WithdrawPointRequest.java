package com.trustamarket.walletservice.wallet.presentation.dto.request;

import jakarta.validation.constraints.Positive;

public record WithdrawPointRequest(
    @Positive long withdrawAmount
) {}
