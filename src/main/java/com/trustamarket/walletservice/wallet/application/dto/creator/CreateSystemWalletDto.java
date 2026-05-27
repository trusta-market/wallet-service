package com.trustamarket.walletservice.wallet.application.dto.creator;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.enums.SystemWalletType;

public record CreateSystemWalletDto(
	UUID operatorId,
	SystemWalletType systemWalletType
) {
	public static CreateSystemWalletDto of(UUID operatorId, SystemWalletType systemWalletType) {
		return new CreateSystemWalletDto(operatorId, systemWalletType);
	}
}
