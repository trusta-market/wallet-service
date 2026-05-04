package com.trustamarket.walletservice.wallet.application.port;

import com.trustamarket.walletservice.wallet.application.dto.result.ChargePointResult;

import java.util.UUID;

public interface PaymentPort {
    ChargePointResult chargePoint(UUID userId, UUID paymentId, long amount);
}
