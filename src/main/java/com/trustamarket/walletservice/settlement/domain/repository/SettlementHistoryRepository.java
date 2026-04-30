package com.trustamarket.walletservice.settlement.domain.repository;

import java.util.UUID;

import com.trustamarket.walletservice.settlement.domain.entity.SettlementHistory;

public interface SettlementHistoryRepository {
	SettlementHistory save(SettlementHistory settlementHistory);
	boolean existsByEventId(UUID eventId);

	void saveAndFlush(SettlementHistory history);
}
