package com.trustamarket.walletservice.wallet.presentation.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WithdrawCompleteRequest(
        @NotNull UUID userId,
		@NotNull UUID payoutId,
        @NotNull UUID pointTxRequestHistoryId,
		@NotNull String payoutStatus,
        @Positive long payoutAmount
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
