package com.trustamarket.walletservice.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trustamarket.walletservice.domain.entity.SettlementHistory;

public interface SettlementHistoryJpaRepository extends JpaRepository<SettlementHistory, UUID> {
	boolean existsByEventId(UUID eventId);
}
