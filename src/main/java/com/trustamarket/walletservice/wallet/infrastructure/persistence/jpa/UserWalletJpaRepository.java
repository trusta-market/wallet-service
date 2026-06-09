package com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trustamarket.walletservice.wallet.domain.entity.UserWallet;

public interface UserWalletJpaRepository extends JpaRepository<UserWallet, UUID> {
	boolean existsByUserId(UUID userId);
	Optional<UserWallet> findByUserId(UUID userId);
}
