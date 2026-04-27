package com.trustamarket.walletservice.domain.repository;

import com.trustamarket.walletservice.domain.entity.Wallet;

public interface WalletRepository {
	Wallet save(Wallet wallet);
}
