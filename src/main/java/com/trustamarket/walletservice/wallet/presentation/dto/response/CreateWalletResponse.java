package com.trustamarket.walletservice.wallet.presentation.dto.response;

import java.util.Objects;
import java.util.UUID;

public record CreateWalletResponse(UUID walletId) {
	public CreateWalletResponse {
		Objects.requireNonNull(walletId, "walletId must not be null");
	}
}
