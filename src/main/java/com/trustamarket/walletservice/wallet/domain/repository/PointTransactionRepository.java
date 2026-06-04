package com.trustamarket.walletservice.wallet.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;
import com.trustamarket.walletservice.wallet.domain.enums.PointTxType;

public interface PointTransactionRepository {
	List<PointTransaction> saveAll(List<PointTransaction> pointTransaction);
	PointTransaction save(PointTransaction chargeTx);

	Slice<PointTransaction> findFirstPointTransactions(UUID walletId, Instant from, Instant to, Pageable pageable);
	Slice<PointTransaction> findNextPointTransactions(UUID walletId, Instant from, Instant to, Instant cursorTime, UUID cursorId, Pageable pageable);

	boolean existsByRefIdAndPointTxType(UUID orderId, PointTxType pointTxType);
	Optional<PointTransaction> findByOrderIdAndWalletIdAndPointTxType(UUID orderId, UUID walletId, PointTxType pointTxType);
	Optional<PointTransaction> findByRefIdAndPointTxType(UUID refId, PointTxType pointTxType);
}
