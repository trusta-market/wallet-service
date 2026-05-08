package com.trustamarket.walletservice.wallet.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.trustamarket.walletservice.wallet.domain.entity.PointTransactionRequestHistory;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRequestHistoryRepository;
import com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa.PointTransactionRequestHistoryJpaRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class PointTransactionRequestHistoryRepositoryImpl implements PointTransactionRequestHistoryRepository {
	private final PointTransactionRequestHistoryJpaRepository pointTransactionRequestHistoryRepository;

	@Override
	public PointTransactionRequestHistory save(PointTransactionRequestHistory requestHitory) {
		return pointTransactionRequestHistoryRepository.save(requestHitory);
	}

	@Override
	public Optional<PointTransactionRequestHistory> findById(UUID pointTxHistoryId) {
		return pointTransactionRequestHistoryRepository.findById(pointTxHistoryId);
	}
}
