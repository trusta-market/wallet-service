package com.trustamarket.walletservice.domain.fee;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FixedFeeRatePolicy implements FeeRatePolicy {

	//MVP 이후 DB로 변경 고려
	@Value("${point.fee.fixedRate:0.05}")
	private BigDecimal rate;

	@Override
	public BigDecimal getRate(UUID sellerId) {
		return rate;
	}
}
