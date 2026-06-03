package com.trustamarket.walletservice.wallet.infrastructure.outbox.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.trustamarket.walletservice.wallet.domain.repository.SystemWalletOutboxRepository;
import com.trustamarket.walletservice.wallet.infrastructure.outbox.OutboxPublisher;
import com.trustamarket.walletservice.wallet.infrastructure.outbox.SystemWalletOutboxProcessor;
import com.trustamarket.walletservice.wallet.infrastructure.outbox.SystemWalletOutboxRelay;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SystemWalletOutboxConfig {
	private final SystemWalletOutboxProcessor processor;
	private final SystemWalletOutboxRepository systemWalletOutboxRepository;

	@Bean
	public OutboxPublisher outboxPublisher() {
		return new SystemWalletOutboxRelay(processor, systemWalletOutboxRepository);
	}

}
