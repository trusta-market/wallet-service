package com.trustamarket.walletservice.wallet.presentation.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WithdrawCompleteRequest(
        @NotNull UUID userId,
		@NotNull UUID paymentId,
        @NotNull UUID pointTxHistoryId,
		@NotNull String payoutStatus,
        @Positive long withdrawAmount
) {
	public static WithdrawCompleteRequest of(
		UUID userId,
		UUID payoutId,
		UUID pointTxRequestHistoryId,
		String payoutStatus,
		long withdrawAmount
	) {
		return new WithdrawCompleteRequest(userId, payoutId, pointTxRequestHistoryId, payoutStatus, withdrawAmount);
	}
}
