package com.trustamarket.walletservice.domain.exception;

import org.springframework.http.HttpStatus;

import com.trustamarket.common.exception.ErrorCodeSpec;

public enum WalletErrorCode implements ErrorCodeSpec {

	ALREADY_EXISTS_WALLET("WALLET_401", HttpStatus.CONFLICT, "이미 지갑이 존재하는 사용자입니다.", "userId");

	WalletErrorCode(String code, HttpStatus httpStatus, String message, String field) {
	}

	@Override
	public String getCode() {
		return "";
	}

	@Override
	public HttpStatus getStatus() {
		return null;
	}

	@Override
	public String getMessage() {
		return "";
	}

	@Override
	public String getField() {
		return "";
	}
}
