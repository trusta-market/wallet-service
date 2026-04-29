package com.trustamarket.walletservice.domain.fee;

import java.math.BigDecimal;

public record FeeCalculation(
	long sellerAmount,
	long feeAmount,
	BigDecimal appliedRate
) {}