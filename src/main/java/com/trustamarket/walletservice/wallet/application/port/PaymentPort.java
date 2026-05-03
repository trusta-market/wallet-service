package com.trustamarket.walletservice.wallet.application.port;

import com.trustamarket.common.response.CommonResponse;
import com.trustamarket.walletservice.wallet.infrastructure.payment.dto.PaymentPointResponse;

import java.util.UUID;

public interface PaymentPort {
    PaymentPointResponse chargePoint(UUID userId, UUID paymentId, long amount);
}
