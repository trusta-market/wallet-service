package com.trustamarket.walletservice.wallet.presentation.dto.request;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.enums.WalletType;

import jakarta.validation.constraints.NotNull;

public record CreateSystemWalletRequest(
	@NotNull UUID operatorId,
	WalletType walletType
) {
	public CreateSystemWalletRequest {

	}
}
