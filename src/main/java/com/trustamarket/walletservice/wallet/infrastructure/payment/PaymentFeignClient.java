package com.trustamarket.walletservice.wallet.infrastructure.payment;

import com.trustamarket.common.response.CommonResponse;
import com.trustamarket.walletservice.wallet.infrastructure.payment.dto.PaymentPointRequest;
import com.trustamarket.walletservice.wallet.infrastructure.payment.dto.PaymentPointResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="payment-service")
public interface PaymentFeignClient {

    @PostMapping("/internal/v1/payments/charges")
    PaymentPointResponse paymentPoint(
            @RequestBody PaymentPointRequest request
    );
}
