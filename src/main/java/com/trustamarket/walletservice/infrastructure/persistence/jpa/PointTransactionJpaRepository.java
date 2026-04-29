package com.trustamarket.walletservice.infrastructure.persistence.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trustamarket.walletservice.domain.entity.PointTransaction;

public interface PointTransactionJpaRepository extends JpaRepository<PointTransaction, UUID> {
}
