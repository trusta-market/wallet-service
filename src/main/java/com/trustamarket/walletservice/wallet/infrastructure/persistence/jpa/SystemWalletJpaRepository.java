package com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trustamarket.walletservice.wallet.domain.entity.SystemWallet;
import com.trustamarket.walletservice.wallet.domain.enums.SystemWalletType;

public interface SystemWalletJpaRepository extends JpaRepository<SystemWallet, UUID> {
	List<SystemWallet> findAllBySystemWalletType(SystemWalletType systemWalletType);
	Optional<SystemWallet> findBySystemWalletType(SystemWalletType systemWalletType);
}
