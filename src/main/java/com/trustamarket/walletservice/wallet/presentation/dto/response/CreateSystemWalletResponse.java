package com.trustamarket.walletservice.wallet.presentation.dto.response;

import java.util.Objects;
import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.entity.Wallet;

public record CreateSystemWalletResponse(
	UUID walletId
) {
	public CreateSystemWalletResponse {
		Objects.requireNonNull(walletId, "walletId must not be null");
	}

	public static CreateSystemWalletResponse from (Wallet wallet) {
		return new CreateSystemWalletResponse(wallet.getWalletId());
	}
}
