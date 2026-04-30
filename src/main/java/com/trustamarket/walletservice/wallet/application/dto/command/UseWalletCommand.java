package com.trustamarket.walletservice.wallet.application.dto.command;

import java.util.UUID;

public record UseWalletCommand(
	UUID orderId,
	UUID buyerId,
	Long totalAmount
) {
	public UseWalletCommand {
		if (orderId == null) {
			throw new IllegalArgumentException("주문 ID는 필수입니다.");
		}
		if (buyerId == null) {
			throw new IllegalArgumentException("구매자 ID는 필수입니다.");
		}
		if (totalAmount == null || totalAmount == 0) {
			throw new IllegalArgumentException("결제 금액은 1원 이상이어야 합니다.");
		}
	}
}
