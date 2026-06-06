package com.trustamarket.walletservice.wallet.domain.exception;

import com.trustamarket.common.exception.CustomException;
import com.trustamarket.common.exception.ErrorCodeSpec;

import lombok.Getter;

@Getter
public class WalletException extends CustomException {
	private final ErrorCodeSpec errorCode;
	private String detail;

	public WalletException(ErrorCodeSpec errorCode) {
		super(errorCode);
		this.errorCode = errorCode;
	}

	public WalletException(String detail, ErrorCodeSpec errorCode) {
		super(errorCode);
		this.detail = detail;
		this.errorCode = errorCode;
	}
}
