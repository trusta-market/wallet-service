package com.trustamarket.walletservice.infrastructure;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.trustamarket.walletservice.domain.entity.SettlementHistory;
import com.trustamarket.walletservice.domain.repository.SettlementHistoryRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class SettlementHistoryRepositoryImpl implements SettlementHistoryRepository {
	private final SettlementHistoryJpaRepository settlementHistoryRepository;

	@Override
	public SettlementHistory save(SettlementHistory settlementHistory) {
		return settlementHistoryRepository.save(settlementHistory);
	}

	@Override
	public boolean existsByEventId(UUID eventId) {
		return settlementHistoryRepository.existsByEventId(eventId);
	}
}
