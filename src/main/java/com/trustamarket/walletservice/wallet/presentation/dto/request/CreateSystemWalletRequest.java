package com.trustamarket.walletservice.wallet.presentation.dto.request;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.enums.SystemWalletType;

import jakarta.validation.constraints.NotNull;

public record CreateSystemWalletRequest(
	@NotNull UUID operatorId,
	SystemWalletType systemWalletType
) {
	public CreateSystemWalletRequest {

	}
}
