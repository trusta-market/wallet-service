package com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa;

import com.trustamarket.walletservice.wallet.domain.entity.UserWallet;
import com.trustamarket.walletservice.wallet.domain.repository.WalletBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserWalletJpaRepository extends JpaRepository<UserWallet, UUID> {
	boolean existsByUserId(UUID userId);
	Optional<UserWallet> findByUserId(UUID userId);

	// 잔액이 불필요한 경로(충전 요청 등)용 — 엔티티 hydration 없이 walletId만 조회.
	@Query("select w.walletId from UserWallet w where w.userId = :userId")
	Optional<UUID> findWalletIdByUserId(@Param("userId") UUID userId);


	// @Modifying 없이 둬야 RETURNING 매핑됨(updateStatusFromRequested 와 동일 패턴).
	@Query(value = "UPDATE p_user_wallets "
		+ "SET point = point + :amount, version = version + 1, updated_at = now() "
		+ "WHERE user_id = :userId AND status = 'ACTIVE' "
		+ "RETURNING wallet_id AS walletId, point AS balance", nativeQuery = true)
	Optional<WalletBalance> increaseBalanceByUserId(@Param("userId") UUID userId, @Param("amount") long amount);


	// read-modify-write + @Version 대신 단일 문장 compare-and-set
	@Query(value = "UPDATE p_user_wallets "
		+ "SET point = point - :amount, version = version + 1, updated_at = now() "
		+ "WHERE user_id = :userId AND status = 'ACTIVE' AND point >= :amount "
		+ "RETURNING wallet_id AS walletId, point AS balance", nativeQuery = true)
	Optional<WalletBalance> decreaseBalanceByUserIdIfEnough(@Param("userId") UUID userId, @Param("amount") long amount);
}
