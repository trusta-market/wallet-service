package com.trustamarket.walletservice.infrastructure;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.trustamarket.walletservice.domain.entity.Wallet;
import com.trustamarket.walletservice.domain.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class WalletRepositoryImpl implements WalletRepository {
	private final WalletJpaRepository walletJpaRepository;

	public Wallet save(Wallet wallet) {
		return walletJpaRepository.save(wallet);
	}

	public boolean existsByUserId(UUID userId) {
		return walletJpaRepository.existsByUserId(userId);
	}
}
