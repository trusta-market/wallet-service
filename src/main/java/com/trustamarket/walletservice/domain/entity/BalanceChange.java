package com.trustamarket.walletservice.domain.entity;

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
		return new BalanceChange(balance, balanceBefore + balance);
	}
}
