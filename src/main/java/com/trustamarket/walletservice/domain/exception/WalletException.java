package com.trustamarket.walletservice.domain.exception;

import org.springframework.http.HttpStatus;

import com.trustamarket.common.exception.CustomException;
import com.trustamarket.common.exception.ErrorCodeSpec;

public class WalletException extends CustomException {
	public WalletException(ErrorCodeSpec errorCode) {
		super(errorCode);
	}

	public WalletException(ErrorCodeSpec errorCode, HttpStatus expected) {
		super(errorCode, expected);
	}

	public WalletException(HttpStatus status, String message) {
		super(status, message);
	}

	public WalletException(HttpStatus status, String message, String field) {
		super(status, message, field);
	}
}
