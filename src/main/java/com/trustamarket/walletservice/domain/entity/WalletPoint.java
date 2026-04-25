package com.trustamarket.walletservice.domain.entity;

import jakarta.persistence.Embeddable;

@Embeddable
record WalletPoint(long point) {
	WalletPoint {
		if (point < 0) {
			throw new IllegalArgumentException("포인트는 0 이상이어야 함. point: " + point);
		}
	}

	static WalletPoint of(long point) {
		return new WalletPoint(point);
	}

	boolean isEnough(long value) {
		if (value < 0) {
			throw new IllegalArgumentException("사용할 포인트 가격도 0 이상이어야 함.");
		}
		return this.point >= value;
	}

	boolean isZero() {
		return this.point == 0;
	}

	WalletPoint increase(long amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("증가 금액은 0보다 커야함 " + amount);
		}
		return new WalletPoint(point + amount);
	}

	WalletPoint decrease(long amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("감소 금액은 0보다 커야함 " + amount);
		}
		if (this.point - amount < 0) {
			throw new IllegalArgumentException("포인트 감소 불가능함 " + (this.point - amount));
		}
		return new WalletPoint(point - amount);
	}


}
