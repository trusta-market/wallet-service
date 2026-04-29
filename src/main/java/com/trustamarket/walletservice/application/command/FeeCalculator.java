package com.trustamarket.walletservice.application.command;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.trustamarket.walletservice.domain.fee.FeeCalculation;
import com.trustamarket.walletservice.domain.fee.FeeRatePolicy;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FeeCalculator {

	private final FeeRatePolicy feeRatePolicy;

	public FeeCalculation calculate(long totalAmount, UUID sellerId) {
		BigDecimal rate = feeRatePolicy.getRate(sellerId);

		long feeAmount = BigDecimal.valueOf(totalAmount)
			.multiply(rate)
			.setScale(0, RoundingMode.DOWN)
			.longValue();

		long sellerAmount = totalAmount - feeAmount;

		return new FeeCalculation(sellerAmount, feeAmount, rate);
	}
}
