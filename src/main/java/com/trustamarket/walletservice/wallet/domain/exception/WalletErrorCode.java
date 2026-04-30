package com.trustamarket.walletservice.wallet.domain.exception;

import org.springframework.http.HttpStatus;

import com.trustamarket.common.exception.ErrorCodeSpec;

public enum WalletErrorCode implements ErrorCodeSpec {

	// WALLET_2XX → 비즈니스 규칙 위반

	// WALLET_3XX → 조회 실패 (NOT_FOUND)
	WALLET_NOT_FOUND("WALLET_301", HttpStatus.NOT_FOUND, "지갑을 찾을 수 없습니다.", "userId"),
	WALLET_NOT_FOUND_BY_TYPE("WALLET_302", HttpStatus.NOT_FOUND, "해당 타입의 지갑을 찾을 수 없습니다.", "walletType"),

	//WALLET_4XX → 생성/중복 관련 (EXISTS)
	ALREADY_EXISTS_WALLET("WALLET_401", HttpStatus.CONFLICT, "이미 지갑이 존재하는 사용자입니다.", "userId");

	// WALLET_5XX → 시스템 에러

	private final String code;
	private final HttpStatus httpStatus;
	private final String message;
	private final String field;

	WalletErrorCode(String code, HttpStatus httpStatus, String message, String field) {
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
