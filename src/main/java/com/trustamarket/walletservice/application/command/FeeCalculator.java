package com.trustamarket.walletservice.application.command;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
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

		if (totalAmount < 0) {
			throw new IllegalArgumentException("정산 금액은 0 이상이어야 합니다.");
		}

		BigDecimal rate = Objects.requireNonNull(feeRatePolicy.getRate(sellerId), "수수료율은 null일 수 없습니다.");
		if (rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
			throw new IllegalArgumentException("수수료율은 0 이상 1 이하여야 합니다.");
		}

		long feeAmount = BigDecimal.valueOf(totalAmount)
			.multiply(rate)
			.setScale(0, RoundingMode.DOWN)
			.longValue();

		long sellerAmount = totalAmount - feeAmount;

		return new FeeCalculation(sellerAmount, feeAmount, rate);
	}
}
