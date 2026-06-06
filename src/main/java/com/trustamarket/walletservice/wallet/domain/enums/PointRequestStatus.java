package com.trustamarket.walletservice.wallet.domain.enums;

public enum PointRequestStatus {
	REQUESTED,
	SUCCESS,
	FAILED,
	INSUFFICIENT;

	public static PointRequestStatus from(String value) {
		if (value == null) {
			throw new IllegalArgumentException("status는 null일 수 없습니다");
		}

		for (PointRequestStatus status : values()) {
			if (status.name().equalsIgnoreCase(value)) {
				return status;
			}
		}

		throw new IllegalArgumentException("올바르지 않은 status: " + value);
	}
}