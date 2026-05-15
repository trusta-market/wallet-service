package com.trustamarket.walletservice.application.command;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trustamarket.walletservice.wallet.application.command.PointTxRequestService;
import com.trustamarket.walletservice.wallet.application.command.WalletCommandServiceImpl;
import com.trustamarket.walletservice.wallet.application.dto.command.WithdrawCompleteCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.WithdrawPointCommand;
import com.trustamarket.walletservice.wallet.application.dto.result.WithdrawPointResult;
import com.trustamarket.walletservice.wallet.application.port.PaymentPort;
import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;
import com.trustamarket.walletservice.wallet.domain.entity.PointTransactionRequestHistory;
import com.trustamarket.walletservice.wallet.domain.entity.Wallet;
import com.trustamarket.walletservice.wallet.domain.enums.PointRequestStatus;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRepository;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRequestHistoryRepository;
import com.trustamarket.walletservice.wallet.domain.repository.WalletRepository;
import com.trustamarket.walletservice.wallet.global.handler.IdempotencyHandler;

@ExtendWith(MockitoExtension.class)
class WalletCommandWithdrawServiceTest {

	@Mock private WalletRepository walletRepository;
	@Mock private PointTransactionRequestHistoryRepository pointTxRequestHistoryRepository;
	@Mock private PointTransactionRepository pointTransactionRepository;
	@Mock private PaymentPort paymentPort;
	@Mock private PointTxRequestService pointTxRequestService;
	@Mock private IdempotencyHandler idempotencyHandler;

	@InjectMocks
	private WalletCommandServiceImpl walletWithdrawService;

	private UUID userId;
	private UUID refId;
	private Wallet wallet;

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
		refId = UUID.randomUUID();
		wallet = Wallet.createUserWallet(userId);
		wallet.chargeComplete(100000L, refId);
	}

	@Test
	@DisplayName("출금 요청 시 요청 이력 저장 후 결제 시스템 호출")
	void withdrawPoint_savesRequestAndCallsPaymentPort() {
		// given
		long amount = 10000L;
		UUID savedHistoryId = UUID.randomUUID();
		String idempotencyKey = UUID.randomUUID().toString();

		WithdrawPointCommand command = new WithdrawPointCommand(userId, idempotencyKey, amount);
		given(pointTxRequestService.withdrawPointRequest(command)).willReturn(savedHistoryId);

		// when
		WithdrawPointResult result = walletWithdrawService.withdrawPoint(command);

		// then
		assertThat(result.pointTxRequestHistoryId()).isEqualTo(savedHistoryId);
		verify(pointTxRequestService).withdrawPointRequest(command);
		verify(paymentPort).withdrawPoint(userId, savedHistoryId, amount);
	}

	@Test
	@DisplayName("신규 idempotency key는 멱등성 체크 후 신규 출금 요청으로 처리된다")
	void withdrawPoint_newIdempotencyKey_proceedsAsNewRequest() {
		// given
		String idempotencyKey = UUID.randomUUID().toString();
		UUID savedHistoryId = UUID.randomUUID();
		long withdrawAmount = 10000L;

		WithdrawPointCommand command = new WithdrawPointCommand(
			userId, idempotencyKey, withdrawAmount
		);

		given(idempotencyHandler.check(idempotencyKey))
			.willReturn(Optional.empty());
		given(pointTxRequestService.withdrawPointRequest(command))
			.willReturn(savedHistoryId);

		// when
		WithdrawPointResult result = walletWithdrawService.withdrawPoint(command);

		// then
		assertThat(result.pointTxRequestHistoryId()).isEqualTo(savedHistoryId);

		verify(idempotencyHandler).check(idempotencyKey);
		verify(pointTxRequestService).withdrawPointRequest(command);
		verify(paymentPort).withdrawPoint(userId, savedHistoryId, withdrawAmount);
	}

	@Test
	@DisplayName("이미 성공한 idempotency key로 재요청 시 기존 historyId를 반환한다")
	void withdrawPoint_alreadySucceeded_returnsExistingHistoryId() {
		// given
		String idempotencyKey = UUID.randomUUID().toString();
		UUID existingHistoryId = UUID.randomUUID();

		PointTransactionRequestHistory existingHistory = mock(PointTransactionRequestHistory.class);
		given(existingHistory.getPointTxRequestHistoryId()).willReturn(existingHistoryId);
		given(idempotencyHandler.check(idempotencyKey))
			.willReturn(Optional.of(existingHistory));

		WithdrawPointCommand command = new WithdrawPointCommand(
			userId, idempotencyKey, 10000L
		);

		// when
		WithdrawPointResult result = walletWithdrawService.withdrawPoint(command);

		// then
		assertThat(result.pointTxRequestHistoryId()).isEqualTo(existingHistoryId);

		verify(pointTxRequestService, never()).withdrawPointRequest(any());
		verify(paymentPort, never()).withdrawPoint(any(), any(), anyLong());
	}

	@Test
	@DisplayName("진행 중이거나 실패한 idempotency key로 재요청 시 예외가 발생한다")
	void withdrawPoint_inProgressOrFailed_throwsException() {
		// given
		String idempotencyKey = UUID.randomUUID().toString();

		given(idempotencyHandler.check(idempotencyKey))
			.willThrow(new RuntimeException("already in progress or failed"));

		WithdrawPointCommand command = new WithdrawPointCommand(
			userId, idempotencyKey, 10000L
		);

		// when & then
		assertThatThrownBy(() -> walletWithdrawService.withdrawPoint(command))
			.isInstanceOf(RuntimeException.class);

		verify(pointTxRequestService, never()).withdrawPointRequest(any());
		verify(paymentPort, never()).withdrawPoint(any(), any(), anyLong());
	}

	// withdrawComplete
	@Test
	@DisplayName("결제 SUCCESS 콜백 시 잔액 차감 및 요청 이력 SUCCESS 처리")
	void withdrawComplete_success() {
		// given
		UUID historyId = UUID.randomUUID();
		UUID paymentId = UUID.randomUUID();
		long requestedAmount = 10000L;
		long actualAmount = 10000L;

		PointTransactionRequestHistory history = mock(PointTransactionRequestHistory.class);
		given(history.getRequestPoint()).willReturn(requestedAmount);

		Wallet walletSpy = spy(wallet);
		PointTransaction withdrawTx = mock(PointTransaction.class);
		doReturn(withdrawTx).when(walletSpy).withdraw(requestedAmount, actualAmount, paymentId);

		given(walletRepository.findByUserId(userId)).willReturn(Optional.of(walletSpy));
		given(pointTxRequestHistoryRepository.findById(historyId)).willReturn(Optional.of(history));

		WithdrawCompleteCommand command = new WithdrawCompleteCommand(
			userId, paymentId, historyId, PointRequestStatus.SUCCESS, actualAmount
		);

		// when
		walletWithdrawService.withdrawComplete(command);

		// then
		verify(walletSpy).withdraw(requestedAmount, actualAmount, paymentId);
		verify(walletRepository).save(walletSpy);
		verify(pointTransactionRepository).save(withdrawTx);
		verify(history).success();
		verify(pointTxRequestHistoryRepository).save(history);
	}

	@Test
	@DisplayName("결제 FAILED 콜백 시 잔액 변경 없이 history만 FAILED 처리")
	void withdrawComplete_failed() {
		UUID historyId = UUID.randomUUID();
		UUID paymentId = UUID.randomUUID();
		String idempotencyKey = UUID.randomUUID().toString();
		long requestedAmount = 10000L;

		Wallet wallet = mock(Wallet.class);
		given(wallet.checkBalance()).willReturn(1000000L);

		PointTransactionRequestHistory history =
			PointTransactionRequestHistory.payoutRequest(wallet, requestedAmount, idempotencyKey);

		given(pointTxRequestHistoryRepository.findById(historyId))
			.willReturn(Optional.of(history));
		given(walletRepository.findByUserId(userId)).willReturn(Optional.of(wallet));


		WithdrawCompleteCommand command = new WithdrawCompleteCommand(
			userId, paymentId, historyId, PointRequestStatus.FAILED, 10000L
		);

		walletWithdrawService.withdrawComplete(command);

		// then
		// 잔액이 그대로인지 -> 한 번도 불리지 않음
		verify(wallet, never()).withdraw(anyLong(), anyLong(), any(UUID.class));

		// history 상태가 FAILED
		assertThat(history.getRequestPoint()).isEqualTo(requestedAmount);
		assertThat(history)
			.extracting("status")
			.isEqualTo(PointRequestStatus.FAILED);

		verify(pointTransactionRepository, never()).save(any());

		verify(walletRepository, never()).save(any());

		verify(pointTxRequestHistoryRepository).save(history);
	}

	@Test
	@DisplayName("지갑이 없으면 예외 발생")
	void withdrawComplete_walletNotFound_throwsException() {
		// given
		UUID historyId = UUID.randomUUID();
		given(walletRepository.findByUserId(userId)).willReturn(Optional.empty());

		WithdrawCompleteCommand command = new WithdrawCompleteCommand(
			userId, UUID.randomUUID(), historyId, PointRequestStatus.SUCCESS, 10000L
		);

		// when & then
		assertThatThrownBy(() -> walletWithdrawService.withdrawComplete(command))
			.isInstanceOf(WalletException.class);

		verify(pointTransactionRepository, never()).save(any());
	}

	@Test
	@DisplayName("요청 이력이 없으면 예외 발생")
	void withdrawComplete_historyNotFound_throwsException() {
		// given
		UUID historyId = UUID.randomUUID();
		given(walletRepository.findByUserId(userId)).willReturn(Optional.of(wallet));
		given(pointTxRequestHistoryRepository.findById(historyId)).willReturn(Optional.empty());

		WithdrawCompleteCommand command = new WithdrawCompleteCommand(
			userId, UUID.randomUUID(), historyId,  PointRequestStatus.SUCCESS, 10000L
		);

		// when & then
		assertThatThrownBy(() -> walletWithdrawService.withdrawComplete(command))
			.isInstanceOf(WalletException.class);

		verify(pointTransactionRepository, never()).save(any());
	}
}