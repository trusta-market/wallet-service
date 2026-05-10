package com.trustamarket.walletservice.wallet.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.entity.PointTransactionRequestHistory;

public interface PointTransactionRequestHistoryRepository {
	PointTransactionRequestHistory save(PointTransactionRequestHistory chargeTx);
	Optional<PointTransactionRequestHistory> findById(UUID pointTxHistoryId);
	boolean existsById(UUID pointTxHistoryId);
}
