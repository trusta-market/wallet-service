package com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;

public interface PointTransactionJpaRepository extends JpaRepository<PointTransaction, UUID> {
	@Query("""
		SELECT t FROM PointTransaction t
		WHERE t.wallet.walletId = :walletId
		  AND t.createdAt >= :from
		  AND t.createdAt <= :to
		ORDER BY t.createdAt DESC, t.pointTransactionId DESC
	""")
	Slice<PointTransaction> findFirstPointTransactions(
		UUID walletId, Instant from, Instant to, Pageable pageable
	);

	@Query("""
		SELECT t FROM PointTransaction t
		WHERE t.wallet.walletId = :walletId
		  AND t.createdAt >= :from
		  AND t.createdAt <= :to
		  AND (t.createdAt < :cursorTime
			   OR (t.createdAt = :cursorTime AND t.pointTransactionId < :cursorId))
		ORDER BY t.createdAt DESC, t.pointTransactionId DESC
	""")
	Slice<PointTransaction> findNextPointTransactions(
		UUID walletId, Instant from, Instant to,
		Instant cursorTime, UUID cursorId, Pageable pageable
	);
}
