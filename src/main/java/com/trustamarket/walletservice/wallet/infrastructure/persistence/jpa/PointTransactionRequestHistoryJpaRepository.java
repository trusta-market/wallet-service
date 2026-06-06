package com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trustamarket.walletservice.wallet.domain.entity.PointTransactionRequestHistory;
import com.trustamarket.walletservice.wallet.domain.enums.PointRequestType;

public interface PointTransactionRequestHistoryJpaRepository extends JpaRepository<PointTransactionRequestHistory, UUID> {
	Optional<PointTransactionRequestHistory> findByIdempotencyKey(String IdempotencyKey);
	Optional<PointTransactionRequestHistory> findByIdempotencyKeyAndRefIdAndRequestType(String IdempotencyKey, UUID refId, PointRequestType requestType);
}
