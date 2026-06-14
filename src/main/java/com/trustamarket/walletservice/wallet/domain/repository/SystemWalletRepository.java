package com.trustamarket.walletservice.wallet.domain.repository;

import com.trustamarket.walletservice.wallet.domain.entity.SystemWallet;
import com.trustamarket.walletservice.wallet.domain.enums.SystemWalletType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SystemWalletRepository {
	SystemWallet save(SystemWallet wallet);
	List<SystemWallet> findAllBySystemWalletType (SystemWalletType systemWalletType);
	Optional<SystemWallet> findBySystemWalletType(SystemWalletType systemWalletType);

	long increaseBalance(UUID walletId, long amount);
	Optional<Long> decreaseBalanceIfSufficient(UUID walletId, long amount);
	long decreaseBalanceUnchecked(UUID walletId, long amount);
}
