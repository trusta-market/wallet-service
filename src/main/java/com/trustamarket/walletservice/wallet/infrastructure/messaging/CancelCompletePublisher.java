package com.trustamarket.walletservice.wallet.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trustamarket.walletservice.wallet.application.dto.event.CancelCompletedEvent;
import com.trustamarket.walletservice.wallet.infrastructure.messaging.message.WalletCancellationCompletedMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelCompletePublisher {

	private static final String TOPIC = "wallet.cancellation.completed";

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void on(CancelCompletedEvent event) {
		objectMapper.registerModule(new JavaTimeModule()); // Instant 처리를 위해 필수
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		try {
			WalletCancellationCompletedMessage payload = WalletCancellationCompletedMessage.of(
				event.orderId(), event.cancelledAmount()
			);

			String json = objectMapper.writeValueAsString(payload);
			kafkaTemplate.send(TOPIC, event.orderId().toString(), json);

			log.info("wallet.cancellation.completed 발행: orderId={}, eventId={}",
				event.orderId(), payload.eventId());

		} catch (JsonProcessingException e) {
			log.error("wallet.cancellation.completed 직렬화 실패: orderId={}", event.orderId(), e);
		} catch (Exception e) {
			log.error("wallet.cancellation.completed 발행 실패: orderId={}, MANUAL RECOVERY NEEDED",
				event.orderId(), e);
		}
	}
}