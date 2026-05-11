package com.trustamarket.walletservice.wallet.infrastructure.messaging;

import static com.trustamarket.walletservice.wallet.domain.entity.PointTransaction.*;
import static com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.trustamarket.walletservice.wallet.application.command.WalletMessageUsecase;
import com.trustamarket.walletservice.wallet.application.dto.message.CancelMessage;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointCancelListener {

	private final WalletMessageUsecase walletMessageUsecase;

	@KafkaListener(
		topics = "order.cancellation.requested",
		groupId = "wallet-cancellation-group"
	)
	public void handle(
		@Payload CancelMessage message,
		@Header(value = "message_id", required = false) String messageId,
		Acknowledgment ack) {
		log.info("정산 요청 수신: orderId={}",
			message.orderId());

		try {
			walletMessageUsecase.cancelProcess(message);
			ack.acknowledge();
		} catch (Exception e) {
			if (isIdempotencyException(e)) {
				log.info("[Idempotency] 중복/동시 이벤트 무시 및 성공 처리", message.orderId());
				ack.acknowledge(); //수동 커밋
				return;
			}

			log.error("정산 처리 중 에러 발생", e);
			throw e;
		}
	}

	private boolean isIdempotencyException(Exception e) {
		// 이미 DB에 있어서 터진 경우
		if (e instanceof WalletException walletException) {
			return walletException.getErrorCode() == ALREADY_CANCELLED;
		}
		/* 동시에 들어와서 DB 유니크 제약조건이 터진 경우
			inbox를 도입하거나 exactly-once를 하더라도 가장 최종적으로 확인되어야 함.
		*/
		if (e instanceof DataIntegrityViolationException de) {
			String msg = de.getMostSpecificCause().getMessage();
			return msg != null && msg.contains(CANCEL_UNIQUE_CONSTRAINT);
		}
		return false;
	}
}
