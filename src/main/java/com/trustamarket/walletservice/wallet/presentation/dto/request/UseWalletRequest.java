package com.trustamarket.walletservice.wallet.presentation.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UseWalletRequest(
	@NotNull
	UUID idempotencyKey, // 결제 요청 ID = idempotencyKey 를 함께 보내줌
	@NotNull
	UUID orderId,
	@NotNull
	UUID buyerId,
	@NotNull
	@Positive(message = "결제 금액은 1원 이상이어야 합니다.")
	Long totalAmount
) {
}
