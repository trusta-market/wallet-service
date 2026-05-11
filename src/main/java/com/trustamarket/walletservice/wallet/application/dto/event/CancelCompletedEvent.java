package com.trustamarket.walletservice.wallet.application.dto.event;

import java.util.UUID;

public record CancelCompletedEvent(
	UUID orderId,
	long cancelledAmount
) {
	public static CancelCompletedEvent of(UUID orderId,  long cancelledAmount) {
		return new CancelCompletedEvent(orderId, cancelledAmount);
	}
}