package com.trustamarket.walletservice.wallet.infrastructure.payment.dto;

import java.util.UUID;

public record PaymentPointRequest(
        UUID userId,
        UUID paymentId,
        long chargeAmount
) {}
