package com.trustamarket.walletservice.domain.enums;

public enum PointTxType {
	CHARGE(Direction.INCREASE),
	PAYMENT(Direction.DECREASE),
	REFUND(null),
	WITHDRAW(Direction.DECREASE),

	SETTLEMENT_IN(Direction.INCREASE),
	SETTLEMENT_OUT(Direction.DECREASE),

	FEE_REVENUE(Direction.INCREASE),
	FEE_REFUND(Direction.DECREASE);

	private final Direction direction;

	PointTxType(Direction direction) {
		this.direction = direction;
	}

	public boolean isIncrease() {
		return direction == Direction.INCREASE;
	}

	public boolean isDecrease() {
		return direction == Direction.DECREASE;
	}

	private enum Direction {
		INCREASE, DECREASE
	}
}
