package com.trustamarket.walletservice.wallet.infrastructure.messaging.message;

import java.time.Instant;
import java.util.UUID;

public record WalletCancellationCompletedMessage(
	UUID    eventId,
	UUID    orderId,
	long    cancelledAmount,
	Instant completedAt
) {
	public static WalletCancellationCompletedMessage of(UUID orderId, long cancelledAmount) {
		return new WalletCancellationCompletedMessage(
			UUID.randomUUID(),
			orderId,
			cancelledAmount,
			Instant.now()
		);
	}
}