package com.trustamarket.walletservice.application.dto.message;

import java.time.Instant;
import java.util.UUID;

public record SettlePointSettlementMessage(
	UUID eventId,
	UUID orderId,
	UUID buyerId,
	UUID sellerId,
	long totalAmount,
	Instant requestedAt
) {
}
