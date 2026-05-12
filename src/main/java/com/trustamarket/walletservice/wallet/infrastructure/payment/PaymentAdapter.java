package com.trustamarket.walletservice.wallet.infrastructure.payment;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.trustamarket.walletservice.wallet.application.port.PaymentPort;
import com.trustamarket.walletservice.wallet.infrastructure.payment.dto.PaymentPointRequest;
import com.trustamarket.walletservice.wallet.infrastructure.payment.dto.WithdrawRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentAdapter implements PaymentPort {

    private final PaymentFeignClient paymentFeignClient;

    @Override
    public void chargePoint(UUID userId, UUID pointTxRequestHistoryId, long chargeAmount) {
        PaymentPointRequest request = new PaymentPointRequest(userId, pointTxRequestHistoryId, chargeAmount);
        paymentFeignClient.paymentPoint(request);
    }

    @Override
    public void withdrawPoint(UUID userId, UUID pointTxRequestHistoryId, long withdrawAmount) {
        WithdrawRequest request = new WithdrawRequest(userId, pointTxRequestHistoryId, withdrawAmount);
        paymentFeignClient.withdraw(request);
    }
}