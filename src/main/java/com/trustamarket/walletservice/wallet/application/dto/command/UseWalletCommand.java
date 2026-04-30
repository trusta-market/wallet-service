package com.trustamarket.walletservice.wallet.application.dto.command;

import java.util.UUID;

public record UseWalletCommand(
	UUID orderId,
	UUID buyerId,
	Long totalAmount
) {
}
