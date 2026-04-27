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
	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseBody
	public ErrorResponse handleDataIntegrity(
		DataIntegrityViolationException e,
		HttpServletRequest request) {
		ErrorResponse errorResponse = ErrorResponse.of(
			HttpStatus.CONFLICT,                        // status
			"Data Integrity Violation",                 // 에러 제목
			"이미 처리 중이거나 존재하는 데이터입니다.",       // 상세 설명
			request.getRequestURI()                     // 에러가 발생한 endpoint
		);
		return errorResponse;
	}
}
