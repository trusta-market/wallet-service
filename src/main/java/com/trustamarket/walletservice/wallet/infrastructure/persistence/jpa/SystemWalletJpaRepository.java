package com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trustamarket.walletservice.wallet.domain.entity.SystemWallet;
import com.trustamarket.walletservice.wallet.domain.enums.SystemWalletType;

public interface SystemWalletJpaRepository extends JpaRepository<SystemWallet, UUID> {
	List<SystemWallet> findAllBySystemWalletType(SystemWalletType systemWalletType);
	Optional<SystemWallet> findBySystemWalletType(SystemWalletType systemWalletType);

	@Modifying(clearAutomatically = true)
	@Query(value = "UPDATE p_system_wallets SET point = point + :amount "
		+ "WHERE wallet_id = :walletId", nativeQuery = true)
	void increaseBalance(@Param("walletId") UUID walletId, @Param("amount") long amount);

	/*
	@Modifying 쿼리가 JPA 스펙상 반환 타입이 int 또는 void
	충분했는지 알기 위해 int형으로 변경
	- 잔액 충분: 1 row 업데이트 → returns 1
	- 잔액 부족: WHERE 조건 불만족 → 0 row 업데이트 → returns 0
	 */
	@Modifying(clearAutomatically = true)
	@Query(value = "UPDATE p_system_wallets set point = point - :amount "
		+ "WHERE wallet_id = :walletId "
		+ "AND point >= :amount", nativeQuery = true)
	int decreaseBalanceIfSufficient(@Param("walletId") UUID walletId, @Param("amount") long amount);

	@Modifying(clearAutomatically = true)
	@Query(value = "UPDATE p_system_wallets set point = point - :amount "
		+ "WHERE wallet_id = :walletId", nativeQuery = true)
	void decreaseBalanceUnchecked(@Param("walletId") UUID walletId, @Param("amount") long amount);
}
