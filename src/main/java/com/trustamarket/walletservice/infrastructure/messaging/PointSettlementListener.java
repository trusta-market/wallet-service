package com.trustamarket.walletservice.infrastructure.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.trustamarket.walletservice.application.command.SettlementCommandUsecase;
import com.trustamarket.walletservice.application.dto.message.SettlePointSettlementMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointSettlementListener {

	private final SettlementCommandUsecase pointSettlementCommandUsecase;

	@KafkaListener(
		topics = "order.wallet-settlement.requested",
		groupId = "wallet-settlement-group"
	)
	public void handle(
		@Payload SettlePointSettlementMessage message,
		@Header("message_id") String messageId,
		Acknowledgment ack) {
		log.info("정산 요청 수신: eventId={}, orderId={}",
			message.eventId(), message.orderId());

		try {
			pointSettlementCommandUsecase.process(message);
			ack.acknowledge();
		} catch (Exception e) {
			log.error("정산 처리 실패: eventId={}", message.eventId(), e);
			throw e;
		}
	}
}
