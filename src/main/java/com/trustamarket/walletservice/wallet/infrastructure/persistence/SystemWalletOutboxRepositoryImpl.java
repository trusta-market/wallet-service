package com.trustamarket.walletservice.wallet.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.trustamarket.walletservice.wallet.domain.entity.SystemWalletOutbox;
import com.trustamarket.walletservice.wallet.domain.enums.OutboxStatus;
import com.trustamarket.walletservice.wallet.domain.repository.SystemWalletOutboxRepository;
import com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa.SystemWalletOutboxJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SystemWalletOutboxRepositoryImpl implements SystemWalletOutboxRepository {
	private final SystemWalletOutboxJpaRepository systemWalletOutboxRepository;

	@Override
	public SystemWalletOutbox save(SystemWalletOutbox systemWalletOutbox) {
		return systemWalletOutboxRepository.save(systemWalletOutbox);
	}

	@Override
	public Optional<SystemWalletOutbox> findById(UUID outboxId) {
		return systemWalletOutboxRepository.findById(outboxId);
	}

	@Override
	public List<SystemWalletOutbox> findAllByOutboxStatusIsPending(Instant before) {
		return systemWalletOutboxRepository.findByOutboxStatusAndCreatedAtBeforeOrderByCreatedAtAsc(OutboxStatus.PENDING, before, PageRequest.of(0, batchSize));
	}
}
