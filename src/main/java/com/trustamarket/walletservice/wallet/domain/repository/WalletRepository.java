package com.trustamarket.walletservice.wallet.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.entity.Wallet;
import com.trustamarket.walletservice.wallet.domain.enums.WalletType;

public interface WalletRepository {
	Wallet save(Wallet wallet);
	boolean existsByUserId(UUID userId);
	Optional<Wallet> findByUserId(UUID userId);
	List<Wallet> findAllByWalletType(WalletType walletType);
	Optional<Wallet> findByWalletType(WalletType walletType);
}
