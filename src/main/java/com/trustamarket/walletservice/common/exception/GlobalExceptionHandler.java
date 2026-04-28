package com.trustamarket.walletservice.common.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.trustamarket.common.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final String WALLET_USER_UNIQUE_CONSTRAINT = "uk_wallet_user_id";

	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseBody
	public ErrorResponse handleDataIntegrity(DataIntegrityViolationException e, HttpServletRequest request) {
		
		String rootMessage = e.getMostSpecificCause().getMessage();
		if (rootMessage == null || !rootMessage.contains(WALLET_USER_UNIQUE_CONSTRAINT)) {
			throw e; // 중복 지갑 제약 위반이 아니면 다른 핸들러/기본 처리로 위임
		}

		ErrorResponse errorResponse = ErrorResponse.of(
			HttpStatus.CONFLICT,                        // status
			"Data Integrity Violation",                 // 에러 제목
			"중복 지갑 생성에서 이미 처리 중이거나 존재하는 데이터입니다.",       // 상세 설명
			request.getRequestURI()                     // 에러가 발생한 endpoint
		);
		return errorResponse;
	}
}
