package com.trustamarket.walletservice.wallet.presentation.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Positive;


public record WithdrawPointRequest(
	UUID pointTxRequestHistoryId,
    @Positive long withdrawAmount
) {}
