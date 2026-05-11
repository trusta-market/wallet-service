package com.trustamarket.walletservice.wallet.application.dto.message;

import java.util.UUID;

public record CancelMessage(
	UUID orderId,
	UUID buyerId,
	long cancelledAmount
) {
	public CancelMessage {
		if (orderId == null) {
			throw new IllegalArgumentException("orderId는 필수입니다.");
		}
		if (buyerId == null) {
			throw new IllegalArgumentException("buyerId 필수입니다.");
		}
		if (cancelledAmount <= 0) {
			throw new IllegalArgumentException("cancelledAmount은 0원 보다 커야 합니다.");
		}
	}
}
