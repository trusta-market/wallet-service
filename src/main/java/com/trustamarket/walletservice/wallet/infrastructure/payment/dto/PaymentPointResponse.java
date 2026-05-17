package com.trustamarket.walletservice.wallet.infrastructure.payment.dto;

import java.time.Instant;
import java.util.UUID;

public record PaymentPointResponse (
        UUID paymentId,
        UUID pointTxRequestHistoryId,
        long chargeAmount,
        Instant createdAt
) {}
