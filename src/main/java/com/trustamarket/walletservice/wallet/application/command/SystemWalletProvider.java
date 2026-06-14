package com.trustamarket.walletservice.wallet.application.command;

import com.trustamarket.walletservice.wallet.domain.entity.SystemWallet;
import com.trustamarket.walletservice.wallet.domain.enums.SystemWalletType;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;
import com.trustamarket.walletservice.wallet.domain.repository.SystemWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode.WALLET_NOT_FOUND_BY_TYPE;

/*
system wallet 종류가 늘어나도 SystemWalletProvider에서 수정
한 종류의 wallet 개수가 늘어났을 때 정책 변경 고려
 */
@Component
@RequiredArgsConstructor
public class SystemWalletProvider {
	private final SystemWalletRepository systemWalletRepository;
	// type별 walletId caching+
	private final Map<SystemWalletType, UUID> walletIdCache = new ConcurrentHashMap<>();

	private UUID getWalletId(SystemWalletType systemWalletType) {
		return walletIdCache.computeIfAbsent(systemWalletType, t -> getByType(t).getWalletId());
	}

	public UUID getPointSourceWalletId() {
		return getWalletId(SystemWalletType.SYSTEM_POINT_SOURCE);
	}
	public UUID getEscrowWalletId() {
		return getWalletId(SystemWalletType.SYSTEM_ESCROW);
	}
	public UUID getFeeWalletId() {
		return getWalletId(SystemWalletType.SYSTEM_FEE);
	}


	public SystemWallet getEscrowWallet() {
		return getByType(SystemWalletType.SYSTEM_ESCROW);
	}
	public SystemWallet getFeeWallet() {
		return getByType(SystemWalletType.SYSTEM_FEE);
	}
	public SystemWallet getPointSourceWallet() {
		return getByType(SystemWalletType.SYSTEM_POINT_SOURCE);
	}

	private SystemWallet getByType(SystemWalletType type) {
		return systemWalletRepository.findBySystemWalletType(type)
			.orElseThrow(() -> new WalletException(WALLET_NOT_FOUND_BY_TYPE));
	}
}

