package com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;

public interface PointTransactionJpaRepository extends JpaRepository<PointTransaction, UUID> {
}
