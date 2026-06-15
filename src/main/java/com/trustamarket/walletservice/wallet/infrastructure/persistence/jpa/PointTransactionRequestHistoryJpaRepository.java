package com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trustamarket.walletservice.wallet.domain.entity.PointTransactionRequestHistory;
import com.trustamarket.walletservice.wallet.domain.enums.PointRequestType;

public interface PointTransactionRequestHistoryJpaRepository extends JpaRepository<PointTransactionRequestHistory, UUID> {
	Optional<PointTransactionRequestHistory> findByIdempotencyKey(String IdempotencyKey);
	Optional<PointTransactionRequestHistory> findByIdempotencyKeyAndRefIdAndRequestType(String IdempotencyKey, UUID refId, PointRequestType requestType);

	// REQUESTED일 때만 status 전이 + request_point 반환. 0행=없음/이미 처리됨.
	// @Modifying 없이 둬야 RETURNING 매핑됨(원자적 compare-and-set).
	@Query(value = "UPDATE p_point_transaction_request_history "
		+ "SET status = :status, updated_at = now() "
		+ "WHERE point_transaction_request_history_id = :id AND status = 'REQUESTED' "
		+ "RETURNING request_point", nativeQuery = true)
	Optional<Long> updateStatusFromRequested(@Param("id") UUID id, @Param("status") String status);
}
