package com.trustamarket.walletservice.wallet.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.entity.UserWallet;

public interface UserWalletRepository {
	UserWallet save(UserWallet userWallet);
	boolean existsByUserId(UUID userId);
	Optional<UserWallet> findByUserId(UUID userId);

	// 잔액이 불필요한 경로용 경량 조회: walletId만 가져오고, 연관관계 저장은 프록시 참조로 처리.
	Optional<UUID> findWalletIdByUserId(UUID userId);
	UserWallet getReferenceByWalletId(UUID walletId);

	// 원자적 잔액 증가. ACTIVE 지갑 없으면 empty.
	Optional<WalletBalance> increaseBalanceByUserId(UUID userId, long amount);
	// 원자적 조건부 차감
	Optional<WalletBalance> decreaseBalanceByUserIdIfEnough(UUID userId, long amount);
}
