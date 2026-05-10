package com.trustamarket.walletservice.wallet.application.dto.query;

import java.time.Instant;
import java.util.UUID;

public record GetPointTransactionQuery(
	Instant from,
	Instant to,
	Instant cursorTime,
	UUID cursorId,
	Integer size
) {
	public static GetPointTransactionQuery of(
		Instant from,
		Instant to,
		Instant cursorTime,
		UUID cursorId,
		Integer size
	) {
		return new GetPointTransactionQuery(from, to, cursorTime, cursorId, size);
	}
}
