package com.trustamarket.walletservice.settlement.domain.fee;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FixedFeeRatePolicy implements FeeRatePolicy {

	//MVP 이후 DB로 변경 고려
	private final BigDecimal rate;

	public FixedFeeRatePolicy(@Value("${point.fee.fixedRate}") BigDecimal rate) {
		if (rate == null || rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
			throw new IllegalArgumentException("point.fee.fixedRate는 0 이상 1 이하여야 합니다.");
		}
		this.rate = rate;
	}

	@Override
	public BigDecimal getRate(UUID sellerId) {
		return rate;
	}
}
