package com.trustamarket.walletservice.wallet.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.trustamarket.walletservice.wallet.domain.entity.PointTransactionRequestHistory;
import com.trustamarket.walletservice.wallet.domain.enums.PointRequestType;
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

	@Override
	public Optional<PointTransactionRequestHistory> findByIdempotencyKey(String IdempotencyKey) {
		return pointTransactionRequestHistoryRepository.findByIdempotencyKey(IdempotencyKey);
	}

	@Override
	public Optional<PointTransactionRequestHistory> findByIdempotencyKeyAndRefIdAndPointRequestType(
		String idempotencyKey, UUID refId, PointRequestType pointRequestType) {
		return pointTransactionRequestHistoryRepository.findByIdempotencyKeyAndRefIdAndRequestType(idempotencyKey, refId, pointRequestType);
	}

	@Override
	public boolean existsById(UUID pointTxHistoryId) {
		return pointTransactionRequestHistoryRepository.existsById(pointTxHistoryId);
	}

	@Override
	public Optional<Long> updateStatusFromRequested(UUID id, String status) {
		return pointTransactionRequestHistoryRepository.updateStatusFromRequested(id, status);
	}
}
