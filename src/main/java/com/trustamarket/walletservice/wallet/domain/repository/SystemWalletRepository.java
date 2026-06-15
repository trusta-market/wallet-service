package com.trustamarket.walletservice.wallet.domain.repository;

import com.trustamarket.walletservice.wallet.domain.entity.SystemWallet;
import com.trustamarket.walletservice.wallet.domain.enums.SystemWalletType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SystemWalletRepository {
	SystemWallet save(SystemWallet wallet);

	Optional<SystemWallet> findById(UUID walletId); // outbox 위해 사용
	List<SystemWallet> findAllBySystemWalletType (SystemWalletType systemWalletType);
	Optional<SystemWallet> findBySystemWalletType(SystemWalletType systemWalletType);
}
