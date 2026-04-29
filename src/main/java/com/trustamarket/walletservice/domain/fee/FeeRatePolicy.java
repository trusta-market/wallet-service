package com.trustamarket.walletservice.domain.fee;

import java.math.BigDecimal;
import java.util.UUID;

public interface FeeRatePolicy {
	BigDecimal getRate(UUID sellerId);
}
