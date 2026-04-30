package com.trustamarket.walletservice.settlement.domain.exception;

import com.trustamarket.common.exception.CustomException;
import com.trustamarket.common.exception.ErrorCodeSpec;

public class SettlementException extends CustomException {
	private final ErrorCodeSpec errorCode;

	public SettlementException(ErrorCodeSpec errorCode) {
		super(errorCode);
		this.errorCode = errorCode;
	}
}
