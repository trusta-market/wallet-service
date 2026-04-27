package com.trustamarket.walletservice.infrastructure;

import org.springframework.stereotype.Repository;

import com.trustamarket.walletservice.domain.entity.Wallet;
import com.trustamarket.walletservice.domain.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class WalletRepositoryImpl implements WalletRepository {
	private final WalletJpaRepository walletJpaRepository;

	public Wallet save(Wallet wallet) {
		walletJpaRepository.save(wallet);
	}
}
