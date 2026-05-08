package com.trustamarket.walletservice.wallet.presentation.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateWalletRequest(
	@NotNull UUID userId
) {
	public static CreateWalletRequest of(UUID userId){
		return new CreateWalletRequest(userId);
	}
}
