package com.trustamarket.walletservice.wallet.domain.enums;

public enum PointTxType {
	CHARGE(Direction.INCREASE),
	BUYER_PAYMENT(Direction.DECREASE),
	ESCROW_DEPOSIT(Direction.INCREASE),
	REFUND(null),
	WITHDRAW(Direction.DECREASE),

	CANCEL_IN(Direction.INCREASE), // user
	CANCEL_OUT(Direction.DECREASE), // system

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
