package com.trustamarket.walletservice.wallet.domain.entity;

import jakarta.persistence.Embeddable;

@Embeddable
record BalanceChange (
	long balance,
	long balanceAfter
) {
	BalanceChange {
		if (balanceAfter < 0) {
			throw new IllegalArgumentException(
				"변경 후 잔액은 0 이상이어야 함. " + balanceAfter
			);
		}
	}

	static BalanceChange of(long balanceBefore, long balance) {
		try {
			long balanceAfter = Math.addExact(balanceBefore, balance);
			return new BalanceChange(balance, balanceAfter);
		} catch (ArithmeticException e) {
			throw new IllegalStateException("포인트 계산 중 오버플로우 발생");
		}
	}
}
