package com.trustamarket.walletservice.domain.repository;

import java.util.UUID;

import com.trustamarket.walletservice.domain.entity.Wallet;

public interface WalletRepository {
	Wallet save(Wallet wallet);
	boolean existsByUserId(UUID userId);
}
