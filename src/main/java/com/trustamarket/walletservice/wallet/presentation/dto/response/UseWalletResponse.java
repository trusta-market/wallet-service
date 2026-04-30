package com.trustamarket.walletservice.wallet.presentation.dto.response;

public record UseWalletResponse(
	Long balance,
	Long shortage
) {
}
