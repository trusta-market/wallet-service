package com.trustamarket.walletservice.wallet.domain.entity;

import jakarta.persistence.Embeddable;

@Embeddable
record BalanceChange (
	long amount,
	Long balanceAfter
) {
	BalanceChange {
		// todo: system point source는 음수 허용 이후 구조 고민 필요
		// if (balanceAfter < 0) {
		// 	throw new IllegalArgumentException(
		// 		"변경 후 잔액은 0 이상이어야 함. " + balanceAfter
		// 	);
		// }
	}

	static BalanceChange of(long balanceBefore, long amount) {
		try {
			long balanceAfter = Math.addExact(balanceBefore, amount);
			return new BalanceChange(amount, balanceAfter);
		} catch (ArithmeticException e) {
			throw new IllegalStateException("포인트 계산 중 오버플로우 발생");
		}
	}

	static BalanceChange of(long amount) {
		return new BalanceChange(amount, null);
	}
}
