package com.trustamarket.walletservice.wallet.infrastructure.payment.dto;

import java.util.UUID;

public record PaymentPointRequest(
        UUID userId,
        UUID paymentId,
        long chargeAmount
) {
    public PaymentPointRequest {
        if (userId == null) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
        if (paymentId == null) {
            throw new IllegalArgumentException("paymentId는 필수입니다.");
        }
        if (chargeAmount < 0) {
            throw new IllegalArgumentException("chargeAmount 필수입니다.");
        }
    }
}
