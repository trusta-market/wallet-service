package com.trustamarket.walletservice.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.trustamarket.walletservice.domain.entity.Wallet;
import com.trustamarket.walletservice.domain.enums.WalletType;
import com.trustamarket.walletservice.domain.repository.WalletRepository;
import com.trustamarket.walletservice.infrastructure.persistence.jpa.WalletJpaRepository;

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

	@Override
	public Optional<Wallet> findByUserId(UUID userId) {
		return walletJpaRepository.findByUserId(userId);
	}

	@Override
	public List<Wallet> findAllByWalletType(WalletType walletType) {
		return walletJpaRepository.findAllByWalletType(walletType);
	}

	@Override
	public Optional<Wallet> findByWalletType(WalletType walletType) {
		return walletJpaRepository.findByWalletType(walletType);
	}
}
