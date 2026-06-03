package com.trustamarket.walletservice.wallet.infrastructure.outbox;

import com.trustamarket.walletservice.wallet.application.dto.event.SystemWalletOutboxEvent;

public interface OutboxPublisher {
	void publish(SystemWalletOutboxEvent outboxEvent);
}
