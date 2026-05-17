package com.trustamarket.walletservice.wallet.application.creator;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.trustamarket.walletservice.wallet.domain.entity.Wallet;
import com.trustamarket.walletservice.wallet.domain.enums.WalletType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SystemPointSourceWalletCreator implements SystemWalletCreator{
	@Override
	public WalletType getType() {
		return WalletType.SYSTEM_POINT_SOURCE;
	}

	@Override
	public Wallet create(UUID operatorId) {
		return Wallet.createSystemPointSourceWallet(operatorId);
	}
}
