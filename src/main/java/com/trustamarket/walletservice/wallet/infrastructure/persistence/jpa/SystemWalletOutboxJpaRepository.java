package com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.trustamarket.walletservice.wallet.domain.entity.SystemWalletOutbox;
import com.trustamarket.walletservice.wallet.domain.enums.OutboxStatus;

public interface SystemWalletOutboxJpaRepository extends JpaRepository<SystemWalletOutbox, UUID> {
	List<SystemWalletOutbox> findByOutboxStatusAndCreatedAtBeforeOrderByCreatedAtAsc(
		OutboxStatus outboxStatus, Instant before, Pageable pageable
	);
}
