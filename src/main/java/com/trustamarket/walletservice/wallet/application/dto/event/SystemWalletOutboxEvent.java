package com.trustamarket.walletservice.wallet.application.dto.event;

import java.util.UUID;

public record SystemWalletOutboxEvent(UUID outboxId) {
	public SystemWalletOutboxEvent {
		if (outboxId == null) {
			throw new IllegalArgumentException("outboxId는 필수입니다.");
		}
	}
	public static SystemWalletOutboxEvent of(UUID outboxId) {
		return new SystemWalletOutboxEvent(outboxId);
	}
}
