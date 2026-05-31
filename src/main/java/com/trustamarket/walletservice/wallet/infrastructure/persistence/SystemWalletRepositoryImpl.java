package com.trustamarket.walletservice.wallet.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.trustamarket.walletservice.wallet.domain.entity.SystemWallet;
import com.trustamarket.walletservice.wallet.domain.enums.SystemWalletType;
import com.trustamarket.walletservice.wallet.domain.repository.SystemWalletRepository;
import com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa.SystemWalletJpaRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class SystemWalletRepositoryImpl implements SystemWalletRepository {
	private final SystemWalletJpaRepository walletJpaRepository;

	public SystemWallet save(SystemWallet wallet) {
		return walletJpaRepository.save(wallet);
	}

	@Override
	public List<SystemWallet> findAllBySystemWalletType(SystemWalletType walletType) {
		return walletJpaRepository.findAllBySystemWalletType(walletType);
	}

	@Override
	public Optional<SystemWallet> findBySystemWalletType(SystemWalletType walletType) {
		return walletJpaRepository.findBySystemWalletType(walletType);
	}

	@Override
	public void increaseBalance(UUID walletId, long amount) {
		walletJpaRepository.increaseBalance(walletId, amount);
	}

	@Override
	public int decreaseBalanceIfSufficient(UUID walletId, long amount) {
		return walletJpaRepository.decreaseBalanceIfSufficient(walletId, amount);
	}

	@Override
	public void decreaseBalanceUnchecked(UUID walletId, long amount) {
		walletJpaRepository.decreaseBalanceUnchecked(walletId, amount);
	}
}
