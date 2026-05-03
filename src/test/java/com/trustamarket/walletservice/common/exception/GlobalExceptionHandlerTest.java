package com.trustamarket.walletservice.common.exception;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import com.trustamarket.common.response.ErrorResponse;
import com.trustamarket.walletservice.wallet.common.exception.GlobalExceptionHandler;

class GlobalExceptionHandlerTest {

	// 테스트할 핸들러 객체 생성 (스프링 컨테이너 안 띄우고 순수 자바로 테스트해서 엄청 빠릅니다!)
	private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

	@Test
	@DisplayName("uk_wallet_user_id 제약 위반 시 409 응답을 반환한다")
	void handleDataIntegrity_walletConstraint_returnsConflict() throws Exception {
		// given
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/internal/wallets");

		Throwable rootCause = new RuntimeException("Duplicate entry 'test-uuid' for key 'uk_wallet_user_id'");
		DataIntegrityViolationException exception = new DataIntegrityViolationException("Execute failed", rootCause);

		// when
		ErrorResponse response = exceptionHandler.handleDataIntegrity(exception, request);

		// then
		assertThat(response.status()).isEqualTo(HttpStatus.CONFLICT.value());
		assertThat(response.detail()).isEqualTo("중복 지갑 생성에서 이미 처리 중이거나 존재하는 데이터입니다.");
		assertThat(response.instance()).isEqualTo("/internal/wallets");
	}

	@Test
	@DisplayName("다른 제약 위반(NOT NULL 등) 시 예외를 낚아채지 않고 다시 던진다")
	void handleDataIntegrity_otherConstraint_rethrows() {
		// given
		MockHttpServletRequest request = new MockHttpServletRequest();

		// Root Cause에 "uk_wallet_user_id"가 없는 다른 에러
		Throwable rootCause = new RuntimeException("Cannot add or update a child row: a foreign key constraint fails ('fk_user_id')");
		DataIntegrityViolationException exception = new DataIntegrityViolationException("Execute failed", rootCause);

		// when & then
		// 우리가 짠 로직을 타면 에러를 처리하지 못하고 그대로 다시 던져야(throw e) 정상
		assertThatThrownBy(() -> exceptionHandler.handleDataIntegrity(exception, request))
			.isInstanceOf(DataIntegrityViolationException.class)
			.hasMessage("Execute failed");
	}
}
