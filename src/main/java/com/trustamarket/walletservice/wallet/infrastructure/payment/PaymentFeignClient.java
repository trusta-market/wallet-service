package com.trustamarket.walletservice.wallet.infrastructure.payment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.trustamarket.common.response.CommonResponse;
import com.trustamarket.walletservice.wallet.infrastructure.payment.dto.PaymentPointRequest;
import com.trustamarket.walletservice.wallet.infrastructure.payment.dto.PaymentPointResponse;
import com.trustamarket.walletservice.wallet.infrastructure.payment.dto.WithdrawRequest;

@RequestMapping("/internal/v1")
@FeignClient(name="payment-service")
public interface PaymentFeignClient {

    @PostMapping("/payments/charges")
    CommonResponse<PaymentPointResponse> paymentPoint(
            @RequestBody PaymentPointRequest request
    );

    @PostMapping("/payouts")
    CommonResponse<Void> withdraw(
        @RequestBody WithdrawRequest request
    );
}
