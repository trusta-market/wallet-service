package com.trustamarket.walletservice.settlement.domain.fee;

import java.math.BigDecimal;
import java.util.UUID;

public interface FeeRatePolicy {
	BigDecimal getRate(UUID sellerId);
}
