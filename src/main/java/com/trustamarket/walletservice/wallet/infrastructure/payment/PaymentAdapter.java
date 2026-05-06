package com.trustamarket.walletservice.wallet.infrastructure.payment;

import com.trustamarket.common.response.CommonResponse;
import com.trustamarket.walletservice.wallet.application.dto.result.ChargePointResult;
import com.trustamarket.walletservice.wallet.application.port.PaymentPort;
import com.trustamarket.walletservice.wallet.infrastructure.payment.dto.PaymentPointRequest;
import com.trustamarket.walletservice.wallet.infrastructure.payment.dto.PaymentPointResponse;
import com.trustamarket.walletservice.wallet.presentation.dto.response.ChargePointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentAdapter implements PaymentPort {

    private final PaymentFeignClient paymentFeignClient;

    @Override
    public ChargePointResult chargePoint(UUID userId, UUID paymentId, long chargeAmount) {
        PaymentPointRequest request = new PaymentPointRequest(userId, paymentId, chargeAmount);

        CommonResponse<PaymentPointResponse> response = paymentFeignClient.paymentPoint(request);

        return new ChargePointResult(response.data().paymentId(), response.data().chargeAmount(), response.data().chargedAt());
    }
}