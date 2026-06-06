package com.trustamarket.walletservice.wallet.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.trustamarket.common.exception.CustomException;
import com.trustamarket.common.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private static final String WALLET_USER_UNIQUE_CONSTRAINT = "uk_wallet_user_id";

	// GlobalExceptionAdvice보다 먼저 잡아서 stack trace 기록 후 동일한 응답 반환
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleCustomExceptionWithTrace(CustomException e, HttpServletRequest request) {
		log.error("[{}] CustomException type={} code={} message={}",
			request.getRequestURI(),
			e.getClass().getSimpleName(),
			e.getType(),
			e.getMessage(),
			e);

		String type = e.getType() != null ? e.getType() : "about:blank";
		ErrorResponse errorResponse = ErrorResponse.of(
			type,
			e.getStatus(),
			e.getStatus().getReasonPhrase(),
			e.getMessage(),
			request.getRequestURI()
		);
		return ResponseEntity.status(e.getStatus()).body(errorResponse);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseBody
	public ErrorResponse handleDataIntegrity(DataIntegrityViolationException e, HttpServletRequest request) {
		String rootMessage = e.getMostSpecificCause().getMessage();
		if (rootMessage == null || !rootMessage.contains(WALLET_USER_UNIQUE_CONSTRAINT)) {
			throw e;
		}

		return ErrorResponse.of(
			HttpStatus.CONFLICT,
			"Data Integrity Violation",
			"중복 지갑 생성에서 이미 처리 중이거나 존재하는 데이터입니다.",
			request.getRequestURI()
		);
	}
}
