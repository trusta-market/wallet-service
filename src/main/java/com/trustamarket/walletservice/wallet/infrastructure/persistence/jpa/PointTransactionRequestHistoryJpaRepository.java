package com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trustamarket.walletservice.wallet.domain.entity.PointTransactionRequestHistory;

public interface PointTransactionRequestHistoryJpaRepository extends JpaRepository<PointTransactionRequestHistory, UUID> {
}
