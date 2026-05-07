package com.trustamarket.walletservice.wallet.presentation.dto.request;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record GetPointTransactionsRequest(
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant cursorTime,
	UUID cursorId,
	@Min(1) @Max(100) Integer size
) {
	public GetPointTransactionsRequest {
		if (size == null) size = 20;
		if (!from.isAfter(to)) {
			throw new IllegalArgumentException("to가 더 최근이여야 함");
		}
		to = getValidTo();
		from = getValidFrom();

		if(!isValidCursor()) {
			throw new IllegalArgumentException("cursor가 맞지 않음");
		}
	}

	public Instant getValidTo() {
		return to != null ? to : Instant.now();
	}

	public Instant getValidFrom() {
		return from != null ? from : getValidTo().minus(30, ChronoUnit.DAYS);
	}

	public boolean isValidCursor() {
		return (cursorTime == null) == (cursorId == null);
	}
}