package com.trustamarket.walletservice.wallet.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.entity.SystemWalletOutbox;

public interface SystemWalletOutboxRepository {
	SystemWalletOutbox save(SystemWalletOutbox systemWalletOutbox);
	Optional<SystemWalletOutbox> findById(UUID outboxId);
	List<SystemWalletOutbox> findAllByOutboxStatusIsPending(Instant before); //for recover
}
