package com.trustamarket.walletservice.application.dto.message;

import java.time.Instant;
import java.util.UUID;

public record SettlePointSettlementMessage(
	UUID eventId,
	UUID orderId,
	UUID buyerId,
	UUID sellerId,
	UUID productId, // 추가 (통계/감사)
	long productPrice, // 추가 (분배 베이스)
	long shippingFee, // 추가 (정산 포함 여부 명확)
	long totalAmount,
	Instant orderConfirmedAt
) {
}
