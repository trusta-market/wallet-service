package com.trustamarket.walletservice.wallet.global.handler;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.trustamarket.walletservice.wallet.domain.entity.PointTransactionRequestHistory;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRequestHistoryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IdempotencyHandler {
	private final PointTransactionRequestHistoryRepository pointTxRequestHistoryRepository;

	public Optional<PointTransactionRequestHistory> check(String idempotencyKey) {
		Optional<PointTransactionRequestHistory> pointTxRequestHistory = pointTxRequestHistoryRepository.findByIdempotencyKey(idempotencyKey);

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
