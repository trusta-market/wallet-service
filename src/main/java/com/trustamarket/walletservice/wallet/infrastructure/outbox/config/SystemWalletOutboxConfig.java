package com.trustamarket.walletservice.wallet.infrastructure.outbox.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import com.trustamarket.walletservice.wallet.domain.repository.SystemWalletOutboxRepository;
import com.trustamarket.walletservice.wallet.infrastructure.outbox.OutboxPublisher;
import com.trustamarket.walletservice.wallet.infrastructure.outbox.SystemWalletOutboxProcessor;
import com.trustamarket.walletservice.wallet.infrastructure.outbox.messageing.SystemWalletOutboxKafkaRelay;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SystemWalletOutboxConfig {
	private final SystemWalletOutboxProcessor processor;
	private final SystemWalletOutboxRepository systemWalletOutboxRepository;

	// @Bean
	// public OutboxPublisher outboxPublisher() {
	// 	return new SystemWalletOutboxRelay(processor, systemWalletOutboxRepository);
	// }

	// Kafka 전환 시 이렇게 교체:
	@Bean
	public OutboxPublisher outboxPublisher(KafkaTemplate<String, String> kafkaTemplate) {
	    return new SystemWalletOutboxKafkaRelay(processor, systemWalletOutboxRepository, kafkaTemplate);
	}
}
