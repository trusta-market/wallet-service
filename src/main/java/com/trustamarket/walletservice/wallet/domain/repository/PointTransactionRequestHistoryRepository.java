package com.trustamarket.walletservice.wallet.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.entity.PointTransactionRequestHistory;
import com.trustamarket.walletservice.wallet.domain.enums.PointRequestType;

public interface PointTransactionRequestHistoryRepository {
	PointTransactionRequestHistory save(PointTransactionRequestHistory chargeTx);
	Optional<PointTransactionRequestHistory> findById(UUID pointTxHistoryId);
	Optional<PointTransactionRequestHistory> findByIdempotencyKey(String IdempotencyKey); //멱등키 저장 관련 고민 필요
	Optional<PointTransactionRequestHistory> findByIdempotencyKeyAndRefIdAndPointRequestType(String IdempotencyKey, UUID refId, PointRequestType pointRequestType);
	boolean existsById(UUID pointTxHistoryId);
}
