package com.trustamarket.walletservice.domain.exception;

import com.trustamarket.common.exception.CustomException;
import com.trustamarket.common.exception.ErrorCodeSpec;

public class WalletException extends CustomException {
	private final ErrorCodeSpec errorCode;

	public WalletException(ErrorCodeSpec errorCode) {
		super(errorCode);
		this.errorCode = errorCode;
	}
}
