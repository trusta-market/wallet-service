package com.trustamarket.walletservice.domain.fee;

import static com.trustamarket.walletservice.domain.exception.SettlementErrorCode.*;

import java.math.BigDecimal;

import com.trustamarket.walletservice.domain.exception.SettlementException;

public record FeeCalculation(
	long sellerAmount,
	long feeAmount,
	BigDecimal appliedRate
) {
	public FeeCalculation {
		if (sellerAmount < 0) {
			throw new IllegalArgumentException(
				"판매자 정산 금액은 음수일 수 없습니다. sellerAmount=" + sellerAmount);
		}
		if (feeAmount < 0) {
			throw new IllegalArgumentException(
				"수수료는 음수일 수 없습니다. feeAmount=" + feeAmount);
		}
		if (appliedRate == null || appliedRate.signum() < 0) {
			throw new IllegalArgumentException(
				"수수료율은 음수일 수 없습니다. appliedRate=" + appliedRate);
		}
	}

	public long total() {
		return sellerAmount + feeAmount;
	}

	public void verifyMatches(long expectedTotal) {
		if (total() != expectedTotal) {
			throw new SettlementException(SETTLEMENT_AMOUNT_MISMATCH);
		}
	}
}