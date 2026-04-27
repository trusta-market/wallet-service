package com.trustamarket.walletservice.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trustamarket.walletservice.domain.entity.Wallet;

public interface WalletJpaRepository extends JpaRepository<Wallet, UUID> {
	boolean existsByUserId(UUID userId);
}
