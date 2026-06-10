package com.trustamarket.walletservice.wallet.global.handler;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.trustamarket.walletservice.wallet.domain.entity.PointTransactionRequestHistory;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRequestHistoryRepository;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyHandler {
	private final PointTransactionRequestHistoryRepository pointTxRequestHistoryRepository;

	@Observed(name = "wallet.idempotency-check")
	public Optional<PointTransactionRequestHistory> check(String idempotencyKey) {
		long t0 = System.currentTimeMillis();
		Optional<PointTransactionRequestHistory> pointTxRequestHistory = pointTxRequestHistoryRepository.findByIdempotencyKey(idempotencyKey);
		log.info("[idempotency-check] {}ms", System.currentTimeMillis() - t0);

		if (pointTxRequestHistory.isEmpty()) {
			return Optional.empty();
		}
		 return Optional.of(isExist(pointTxRequestHistory.get()));
	}

	private PointTransactionRequestHistory isExist(PointTransactionRequestHistory requestHistory) {
		if(requestHistory.isFail()) {
			throw new RuntimeException(String.valueOf(requestHistory.getPointTxRequestHistoryId()));
		}
		if (requestHistory.isRequested()) {
			throw new RuntimeException(String.valueOf(requestHistory.getPointTxRequestHistoryId()));
		}

		return requestHistory;
	}
}
