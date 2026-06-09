package com.trustamarket.walletservice.wallet.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.entity.UserWallet;

public interface UserWalletRepository {
	UserWallet save(UserWallet userWallet);
	boolean existsByUserId(UUID userId);
	Optional<UserWallet> findByUserId(UUID userId);
}
