package com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa;

import com.trustamarket.walletservice.wallet.domain.entity.SystemWallet;
import com.trustamarket.walletservice.wallet.domain.enums.SystemWalletType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SystemWalletJpaRepository extends JpaRepository<SystemWallet, UUID> {
	List<SystemWallet> findAllBySystemWalletType(SystemWalletType systemWalletType);
	Optional<SystemWallet> findBySystemWalletType(SystemWalletType systemWalletType);

	// 모두 UPDATE ... RETURNING point → 갱신 후 잔액 반환
	@Query(value = "UPDATE p_system_wallets SET point = point + :amount "
		+ "WHERE wallet_id = :walletId RETURNING point", nativeQuery = true)
	long increaseBalance(@Param("walletId") UUID walletId, @Param("amount") long amount);

	// 잔액 가드: 충분하면 갱신 후 잔액(Optional.of), 부족하면 0행 → Optional.empty()
	@Query(value = "UPDATE p_system_wallets set point = point - :amount "
		+ "WHERE wallet_id = :walletId "
		+ "AND point >= :amount RETURNING point", nativeQuery = true)
	Optional<Long> decreaseBalanceIfSufficient(@Param("walletId") UUID walletId, @Param("amount") long amount);

	@Query(value = "UPDATE p_system_wallets set point = point - :amount "
		+ "WHERE wallet_id = :walletId RETURNING point", nativeQuery = true)
	long decreaseBalanceUnchecked(@Param("walletId") UUID walletId, @Param("amount") long amount);
}
