package com.trustamarket.walletservice.wallet.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.trustamarket.walletservice.wallet.domain.entity.UserWallet;
import com.trustamarket.walletservice.wallet.domain.repository.UserWalletRepository;
import com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa.UserWalletJpaRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class UserWalletRepositoryImpl implements UserWalletRepository {
	private final UserWalletJpaRepository walletJpaRepository;

	public UserWallet save(UserWallet wallet) {
		return walletJpaRepository.save(wallet);
	}

	public boolean existsByUserId(UUID userId) {
		return walletJpaRepository.existsByUserId(userId);
	}

	@Override
	public Optional<UserWallet> findByUserId(UUID userId) {
		return walletJpaRepository.findByUserId(userId);
	}

	@Override
	public Optional<UUID> findWalletIdByUserId(UUID userId) {
		return walletJpaRepository.findWalletIdByUserId(userId);
	}

	@Override
	public UserWallet getReferenceByWalletId(UUID walletId) {
		return walletJpaRepository.getReferenceById(walletId);
	}
}
