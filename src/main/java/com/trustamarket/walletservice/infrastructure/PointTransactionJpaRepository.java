package com.trustamarket.walletservice.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trustamarket.walletservice.domain.entity.PointTransaction;

public interface PointTransactionJpaRepository extends JpaRepository<PointTransaction, UUID> {
}
