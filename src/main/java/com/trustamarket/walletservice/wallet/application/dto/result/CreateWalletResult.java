package com.trustamarket.walletservice.wallet.application.dto.result;

import java.util.UUID;

public record CreateWalletResult(
	UUID walletId,
	boolean result
) {
}
