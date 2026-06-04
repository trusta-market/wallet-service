package com.trustamarket.walletservice.wallet.infrastructure.outbox.messageing;

import java.time.Instant;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.trustamarket.walletservice.wallet.application.dto.event.SystemWalletOutboxEvent;
import com.trustamarket.walletservice.wallet.domain.entity.SystemWalletOutbox;
import com.trustamarket.walletservice.wallet.domain.enums.OutboxStatus;
import com.trustamarket.walletservice.wallet.domain.repository.SystemWalletOutboxRepository;
import com.trustamarket.walletservice.wallet.infrastructure.outbox.OutboxPublisher;
import com.trustamarket.walletservice.wallet.infrastructure.outbox.SystemWalletOutboxProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SystemWalletOutboxKafkaRelay implements OutboxPublisher {
	private static final String TOPIC = "wallet.system-outbox";

	private final SystemWalletOutboxProcessor processor;
	private final SystemWalletOutboxRepository systemWalletOutboxRepository;
	private final KafkaTemplate<String, String> kafkaTemplate;

	@Override
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publish(SystemWalletOutboxEvent outboxEvent) {
		kafkaTemplate.send(TOPIC, outboxEvent.walletId().toString(), outboxEvent.outboxId().toString());
	}

	@KafkaListener(topics = TOPIC, groupId = "system-wallet-outbox-consumer")
	@Transactional
	public void consume(@Payload String outboxIdPayload, Acknowledgment ack) {
		UUID outboxId = UUID.fromString(outboxIdPayload);
		SystemWalletOutbox outbox = systemWalletOutboxRepository.findById(outboxId)
			.orElseThrow(() -> new IllegalArgumentException("outbox not found: " + outboxId));

		if (outbox.getOutboxStatus() != OutboxStatus.PENDING) {
			ack.acknowledge();
			return;
		}
		processor.apply(outbox.getOutboxId());
		ack.acknowledge();
	}

	@Scheduled(fixedDelay = 5000)
	public void recover() {
		systemWalletOutboxRepository.findAllByOutboxStatusIsPending(Instant.now().minusSeconds(10))
			.forEach(outbox -> kafkaTemplate.send(TOPIC, outbox.getWalletId().toString(),  outbox.getOutboxId().toString()));
	}
}
