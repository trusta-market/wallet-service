package com.trustamarket.walletservice.wallet.application.creator;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.trustamarket.walletservice.wallet.domain.entity.SystemWallet;
import com.trustamarket.walletservice.wallet.domain.enums.SystemWalletType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SystemPointSourceWalletCreator implements SystemWalletCreator{
	@Override
	public SystemWalletType getType() {
		return SystemWalletType.SYSTEM_POINT_SOURCE;
	}

	@Override
	public SystemWallet create(UUID operatorId) {
		return SystemWallet.createSystemPointSourceWallet(operatorId);
	}
}
