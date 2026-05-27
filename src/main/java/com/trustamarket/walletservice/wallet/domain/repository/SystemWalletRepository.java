package com.trustamarket.walletservice.wallet.domain.repository;

import java.util.List;
import java.util.Optional;

import com.trustamarket.walletservice.wallet.domain.entity.SystemWallet;
import com.trustamarket.walletservice.wallet.domain.enums.SystemWalletType;

public interface SystemWalletRepository {
	SystemWallet save(SystemWallet wallet);
	List<SystemWallet> findAllBySystemWalletType (SystemWalletType systemWalletType);
	Optional<SystemWallet> findBySystemWalletType(SystemWalletType systemWalletType);
}
