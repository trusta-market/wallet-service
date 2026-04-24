package com.trusta_market.walllet_service.domain.enums;

public enum PointTxType {
	CHARGE(Direction.INCREASE),
	PAYMENT(Direction.DECREASE),
	REFUND(Direction.INCREASE),
	WITHDRAW(Direction.DECREASE),
	SETTLEMENT(Direction.DECREASE);

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
