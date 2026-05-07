package com.trustamarket.walletservice.wallet.presentation.dto.response;

import java.util.Objects;
import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.entity.Wallet;

public record CreateWalletResponse(UUID walletId) {
	public CreateWalletResponse {
		Objects.requireNonNull(walletId, "walletId must not be null");
	}

	public static CreateWalletResponse from (Wallet wallet) {
		return new CreateWalletResponse(wallet.getWalletId());
	}
}
