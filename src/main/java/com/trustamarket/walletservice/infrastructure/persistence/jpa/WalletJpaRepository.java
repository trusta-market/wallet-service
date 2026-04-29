package com.trustamarket.walletservice.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trustamarket.walletservice.domain.entity.Wallet;
import com.trustamarket.walletservice.domain.enums.WalletType;

public interface WalletJpaRepository extends JpaRepository<Wallet, UUID> {
	boolean existsByUserId(UUID userId);

	Optional<Wallet> findByUserId(UUID userId);
	List<Wallet> findAllByWalletType(WalletType walletType);
	Optional<Wallet> findByWalletType(WalletType walletType);
}
