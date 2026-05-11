package com.trustamarket.walletservice.wallet.infrastructure.payment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.trustamarket.common.response.CommonResponse;
import com.trustamarket.walletservice.wallet.infrastructure.payment.dto.PaymentPointRequest;
import com.trustamarket.walletservice.wallet.infrastructure.payment.dto.PaymentPointResponse;
import com.trustamarket.walletservice.wallet.infrastructure.payment.dto.WithdrawRequest;

@FeignClient(name = "payment-service", path = "/internal/v1")
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
