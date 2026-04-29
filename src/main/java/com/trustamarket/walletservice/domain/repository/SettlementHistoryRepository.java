package com.trustamarket.walletservice.domain.repository;

import java.util.UUID;

import com.trustamarket.walletservice.domain.entity.SettlementHistory;

public interface SettlementHistoryRepository {
	SettlementHistory save(SettlementHistory settlementHistory);
	boolean existsByEventId(UUID eventId);
}
