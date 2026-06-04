package com.trustamarket.walletservice.wallet.application.dto.event;

import java.util.UUID;

public record SystemWalletOutboxEvent(UUID outboxId, UUID walletId) {
	public SystemWalletOutboxEvent {
		if (outboxId == null) {
			throw new IllegalArgumentException("outboxId는 필수입니다.");
		}
		if (walletId == null) {
			throw new IllegalArgumentException("outboxId는 필수입니다.");
		}
	}
	public static SystemWalletOutboxEvent of(UUID outboxId, UUID walletId) {
		return new SystemWalletOutboxEvent(outboxId, walletId);
	}
}
