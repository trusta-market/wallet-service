package com.trustamarket.walletservice.settlement.infrastructure.persistence.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trustamarket.walletservice.settlement.domain.entity.SettlementHistory;

public interface SettlementHistoryJpaRepository extends JpaRepository<SettlementHistory, UUID> {
	boolean existsByEventId(UUID eventId);
}
