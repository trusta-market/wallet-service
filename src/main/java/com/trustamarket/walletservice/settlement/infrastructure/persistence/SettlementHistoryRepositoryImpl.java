package com.trustamarket.walletservice.settlement.infrastructure.persistence;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.trustamarket.walletservice.settlement.domain.entity.SettlementHistory;
import com.trustamarket.walletservice.settlement.domain.repository.SettlementHistoryRepository;
import com.trustamarket.walletservice.settlement.infrastructure.persistence.jpa.SettlementHistoryJpaRepository;

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

	@Override
	public void saveAndFlush(SettlementHistory history) {
		settlementHistoryRepository.saveAndFlush(history);
	}
}
