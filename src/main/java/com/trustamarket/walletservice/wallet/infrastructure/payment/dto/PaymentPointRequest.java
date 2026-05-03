package com.trustamarket.walletservice.wallet.infrastructure.payment.dto;

import java.util.UUID;

public record PaymentPointRequest(
        UUID userID,
        UUID paymentID,
        long chargeamount
) {}
