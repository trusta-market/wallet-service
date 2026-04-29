package com.trustamarket.walletservice.application.command;

import static com.trustamarket.walletservice.domain.exception.WalletErrorCode.*;

import org.springframework.stereotype.Component;

import com.trustamarket.walletservice.domain.entity.Wallet;
import com.trustamarket.walletservice.domain.enums.WalletType;
import com.trustamarket.walletservice.domain.exception.WalletException;
import com.trustamarket.walletservice.domain.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

/*
system wallet 종류가 늘어나도 SystemWalletProvider에서 수정
한 종류의 wallet 개수가 늘어났을 때 정책 변경 고려
 */
@Component
@RequiredArgsConstructor
public class SystemWalletProvider {
	private final WalletRepository walletRepository;

	public Wallet getEscrowWallet() {
		return getByType(WalletType.SYSTEM_ESCROW);
	}

	public Wallet getFeeWallet() {
		return getByType(WalletType.SYSTEM_FEE);
	}

	private Wallet getByType(WalletType type) {
		return walletRepository.findByWalletType(type)
			.orElseThrow(() -> new WalletException(WALLET_NOT_FOUND_BY_TYPE));
	}
}
