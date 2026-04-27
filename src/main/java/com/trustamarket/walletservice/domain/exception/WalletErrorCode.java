package com.trustamarket.walletservice.domain.exception;

import org.springframework.http.HttpStatus;

import com.trustamarket.common.exception.ErrorCodeSpec;

public enum WalletErrorCode implements ErrorCodeSpec {

	ALREADY_EXISTS_WALLET("WALLET_401", HttpStatus.CONFLICT, "이미 지갑이 존재하는 사용자입니다.", "userId");

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
