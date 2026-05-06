package com.trustamarket.walletservice.wallet.application.dto.result;

import java.time.Instant;
import java.util.UUID;

public record ChargePointResult (
        UUID paymentId,
        long chargedAmount,
        Instant chargedAt
) {}
