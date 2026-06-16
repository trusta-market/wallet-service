package com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trustamarket.walletservice.wallet.domain.entity.UserWallet;

public interface UserWalletJpaRepository extends JpaRepository<UserWallet, UUID> {
	boolean existsByUserId(UUID userId);
	Optional<UserWallet> findByUserId(UUID userId);

	// 잔액이 불필요한 경로(충전 요청 등)용 — 엔티티 hydration 없이 walletId만 조회.
	@Query("select w.walletId from UserWallet w where w.userId = :userId")
	Optional<UUID> findWalletIdByUserId(@Param("userId") UUID userId);
}
