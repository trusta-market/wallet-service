package com.trustamarket.walletservice.wallet.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;

public interface PointTransactionRepository {
	List<PointTransaction> saveAll(List<PointTransaction> pointTransaction);
	PointTransaction save(PointTransaction chargeTx);

	Slice<PointTransaction> findFirstPointTransactions(UUID walletId, Instant from, Instant to, Pageable pageable);
	Slice<PointTransaction> findNextPointTransactions(UUID walletId, Instant from, Instant to, Instant cursorTime, UUID cursorId, Pageable pageable);
}
