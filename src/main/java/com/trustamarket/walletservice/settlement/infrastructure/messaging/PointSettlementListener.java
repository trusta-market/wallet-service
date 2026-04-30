package com.trustamarket.walletservice.settlement.infrastructure.messaging;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.trustamarket.walletservice.settlement.application.command.SettlementCommandUsecase;
import com.trustamarket.walletservice.settlement.application.dto.message.SettlePointSettlementMessage;
import com.trustamarket.walletservice.settlement.domain.entity.SettlementHistory;
import com.trustamarket.walletservice.settlement.domain.exception.SettlementErrorCode;
import com.trustamarket.walletservice.settlement.domain.exception.SettlementException;

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
			if (isIdempotencyException(e)) {
				log.info("[Idempotency] 중복/동시 이벤트 무시 및 성공 처리", message.eventId());
				ack.acknowledge(); //수동 커밋
				return;
			}

			log.error("정산 처리 중 에러 발생", e);
			throw e;
		}
	}

	private boolean isIdempotencyException(Exception e) {
		// 이미 DB에 있어서 SettlementException이 터진 경우
		if (e instanceof SettlementException se) {
			return se.getErrorCode() == SettlementErrorCode.ALREADY_SETTLED;
		}
		/* 동시에 들어와서 DB 유니크 제약조건이 터진 경우
			inbox를 도입하거나 exactly-once를 하더라도 가장 최종적으로 확인되어야 함.

		*/
		if (e instanceof DataIntegrityViolationException de) {
			String msg = de.getMostSpecificCause().getMessage();
			return msg != null && msg.contains(SettlementHistory.UK_EVENT_ID);
		}
		return false;
	}
}
