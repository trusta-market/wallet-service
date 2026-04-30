package com.trustamarket.walletservice.settlement.domain.entity;

import jakarta.persistence.Embeddable;

@Embeddable
record SettlementAmount(
	long totalAmount,
	long sellerAmount,
	long feeRevenueAmount) {
	SettlementAmount {
		if (totalAmount < 0 || sellerAmount < 0 || feeRevenueAmount < 0) {
			throw new IllegalArgumentException("정산 금액은 음수일 수 없습니다.");
		}
		try {
			if (Math.addExact(sellerAmount, feeRevenueAmount) != totalAmount) {
				throw new IllegalArgumentException("정산 합계가 분배 합계와 일치해야 합니다.");
			}
		} catch (ArithmeticException e) {
			throw new IllegalStateException("정산 중 오버플로우 발생", e);
		}
	}

	static SettlementAmount of(long totalAmount, long sellerAmount, long feeRevenueAmount) {
		return new SettlementAmount(totalAmount, sellerAmount, feeRevenueAmount);
	}

}
