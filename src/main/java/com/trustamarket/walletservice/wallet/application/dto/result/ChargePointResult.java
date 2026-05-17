package com.trustamarket.walletservice.wallet.application.dto.result;

import java.util.UUID;

public record ChargePointResult (
        UUID pointTxRequestHistoryId
) {}
