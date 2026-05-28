package com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;
import com.trustamarket.walletservice.wallet.domain.enums.PointTxType;

public interface PointTransactionJpaRepository extends JpaRepository<PointTransaction, UUID> {
	@Query("""
		SELECT t FROM PointTransaction t
		WHERE t.walletId = :walletId
		  AND t.createdAt >= :from
		  AND t.createdAt <= :to
		ORDER BY t.createdAt DESC, t.pointTransactionId DESC
	""")
	Slice<PointTransaction> findFirstPointTransactions(
		@Param("walletId") UUID walletId,
		@Param("from") Instant from,
		@Param("to") Instant to,
		Pageable pageable
	);

	@Query("""
		SELECT t FROM PointTransaction t
		WHERE t.walletId = :walletId
		  AND t.createdAt >= :from
		  AND t.createdAt <= :to
		  AND (t.createdAt < :cursorTime
			   OR (t.createdAt = :cursorTime AND t.pointTransactionId < :cursorId))
		ORDER BY t.createdAt DESC, t.pointTransactionId DESC
	""")
	Slice<PointTransaction> findNextPointTransactions(
		@Param("walletId") UUID walletId,
		@Param("from") Instant from,
		@Param("to") Instant to,
		@Param("cursorTime") Instant cursorTime,
		@Param("cursorId") UUID cursorId,
		Pageable pageable
	);

	@Query("""
		SELECT COUNT(pt) > 0 FROM PointTransaction pt
		WHERE pt.ref.refId = :refId
		  AND pt.pointTxType = :pointTxType
    """)
	boolean existsByRefIdAndPointTxType(@Param("refId") UUID refId, @Param("pointTxType") PointTxType pointTxType);

	@Query("""
    SELECT pt FROM PointTransaction pt
    WHERE pt.ref.refId = :orderId
      AND pt.walletId = :walletId
      AND pt.pointTxType = :pointTxType
    """)
	Optional<PointTransaction> findByOrderIdAndWalletIdAndPointTxType(@Param("orderId") UUID orderId, @Param("walletId") UUID walletId, @Param("pointTxType")PointTxType pointTxType);

	@Query("""
    SELECT pt FROM PointTransaction pt
    WHERE pt.ref.refId = :refId
      AND pt.pointTxType = :pointTxType
    """)
	Optional<PointTransaction> findByRefIdAndPointTxType(@Param("refId") UUID refId, @Param("pointTxType") PointTxType pointTxType);
}
