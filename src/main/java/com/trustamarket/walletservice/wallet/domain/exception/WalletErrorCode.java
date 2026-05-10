package com.trustamarket.walletservice.wallet.domain.exception;

import org.springframework.http.HttpStatus;

import com.trustamarket.common.exception.ErrorCodeSpec;

public enum WalletErrorCode implements ErrorCodeSpec {

	// WALLET_2XX → 비즈니스 규칙 위반
	INVALID_DEDUCTION_AMOUNT("WALLET_201", HttpStatus.BAD_REQUEST, "차감 금액은 0보다 커야 합니다.", "amount"),
	INVALID_STATUS_TRANSITION("WALLET_202", HttpStatus.BAD_REQUEST, "INVALID_STATUS_TRANSITION", null),
	INVALID_BALANCE("WALLET_203", HttpStatus.BAD_REQUEST, "잔액 부족", "balance"),
	SYSTEM_WALLET_WITHDRAWAL_NOT_ALLOWED("WALLET_204", HttpStatus.FORBIDDEN, "시스템 계정은 출금할 수 없습니다.", "walletType"),

	// WALLET_3XX → 조회 실패 (NOT_FOUND)
	WALLET_NOT_FOUND("WALLET_301", HttpStatus.NOT_FOUND, "지갑을 찾을 수 없습니다.", "userId"),
	WALLET_NOT_FOUND_BY_TYPE("WALLET_302", HttpStatus.NOT_FOUND, "해당 타입의 지갑을 찾을 수 없습니다.", "walletType"),
	WALLET_POINT_TX_REQUEST_NOT_FOUND("WALLET_303", HttpStatus.NOT_FOUND, "해당 요청 기록을 찾을 수 없습니다.", "pointTxRequestHistoryId"),

	//WALLET_4XX → 생성/중복 관련 (EXISTS)
	ALREADY_EXISTS_WALLET("WALLET_401", HttpStatus.CONFLICT, "이미 지갑이 존재하는 사용자입니다.", "userId"),
	ALREADY_EXISTS_POINT_TX_REQUEST("WALLET_402", HttpStatus.CONFLICT, "해당 요청 기록이 이미 있습니다.", "pointTxRequestHistoryId");
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
