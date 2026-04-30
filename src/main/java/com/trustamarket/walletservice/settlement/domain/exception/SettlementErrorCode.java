package com.trustamarket.walletservice.settlement.domain.exception;

import org.springframework.http.HttpStatus;

import com.trustamarket.common.exception.ErrorCodeSpec;

public enum SettlementErrorCode implements ErrorCodeSpec {

	// _2XX → 비즈니스 규칙 위반
	SETTLEMENT_AMOUNT_MISMATCH("SETTLEMENT_201", HttpStatus.CONFLICT, "정산 금액이 일치하지 않습니다.", null),

	// _3XX → 조회 실패 (NOT_FOUND)

	//_4XX → 생성/중복 관련 (EXISTS)
	ALREADY_SETTLED("SETTLEMENT_401", HttpStatus.CONFLICT, "이미 정산이 완료된 이벤트입니다.", "eventId");

	// _5XX → 시스템 에러

	private final String code;
	private final HttpStatus httpStatus;
	private final String message;
	private final String field;

	SettlementErrorCode(String code, HttpStatus httpStatus, String message, String field) {
		this.code = code;
		this.httpStatus = httpStatus;
		this.message = message;
		this.field = field;
	}

	@Override
	public String getCode() {
		return this.code;
	}

	@Override
	public HttpStatus getStatus() {
		return this.httpStatus;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public String getField() {
		return this.field;
	}
}
