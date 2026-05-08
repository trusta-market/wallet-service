package com.trustamarket.walletservice.wallet.application.port;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.application.dto.result.ChargePointResult;

public interface PaymentPort {
    ChargePointResult chargePoint(UUID userId, UUID paymentId, long amount);
    void withdrawPoint(UUID userId, UUID pointTxRequestHitoryId, long withdrawAmount);
}
