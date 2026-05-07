package com.trustamarket.walletservice.wallet.application.dto.creator;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.enums.WalletType;

public record CreateSystemWalletDto(
	UUID operatorId,
	WalletType walletType
) {
	public static CreateSystemWalletDto of(UUID operatorId, WalletType walletType) {
		return new CreateSystemWalletDto(operatorId, walletType);
	}
}
