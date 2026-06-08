package com.trustamarket.walletservice.wallet.application.command;

import static com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.observation.annotation.Observed;
import org.springframework.util.StopWatch;

import com.trustamarket.walletservice.wallet.application.dto.command.ChargeCompleteCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.ChargePointCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.UseWalletCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.WithdrawCompleteCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.WithdrawPointCommand;
import com.trustamarket.walletservice.wallet.application.dto.event.SystemWalletOutboxEvent;
import com.trustamarket.walletservice.wallet.application.dto.result.ChargePointResult;
import com.trustamarket.walletservice.wallet.application.dto.result.CreateWalletResult;
import com.trustamarket.walletservice.wallet.application.dto.result.UseWalletResult;
import com.trustamarket.walletservice.wallet.application.dto.result.WithdrawPointResult;
import com.trustamarket.walletservice.wallet.application.port.PaymentPort;
import com.trustamarket.walletservice.wallet.domain.entity.PointShortage;
import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;
import com.trustamarket.walletservice.wallet.domain.entity.PointTransactionRequestHistory;
import com.trustamarket.walletservice.wallet.domain.entity.SystemWallet;
import com.trustamarket.walletservice.wallet.domain.entity.SystemWalletOutbox;
import com.trustamarket.walletservice.wallet.domain.entity.UserWallet;
import com.trustamarket.walletservice.wallet.domain.enums.PointRequestStatus;
import com.trustamarket.walletservice.wallet.domain.enums.PointRequestType;
import com.trustamarket.walletservice.wallet.domain.enums.PointTxType;
import com.trustamarket.walletservice.wallet.domain.enums.RefType;
import com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRepository;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRequestHistoryRepository;
import com.trustamarket.walletservice.wallet.domain.repository.SystemWalletOutboxRepository;
import com.trustamarket.walletservice.wallet.domain.repository.UserWalletRepository;
import com.trustamarket.walletservice.wallet.global.handler.IdempotencyHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletCommandServiceImpl implements WalletCommandService {

	private final UserWalletRepository userWalletRepository;
	private final SystemWalletOutboxRepository systemWalletOutboxRepository;
	private final SystemWalletProvider systemWalletProvider;
	private final PaymentPort paymentPort;

	private final PointTransactionRepository pointTransactionRepository;
	private final PointTransactionRequestHistoryRepository pointTxRequestHistoryRepository;

	private final PointTxRequestService pointTxRequestService;
	private final IdempotencyHandler idempotencyHandler;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Observed(name = "wallet.create-wallet")
	@Transactional
	public CreateWalletResult createUserWallet(UUID userId) {
		if (userId == null) {
			// throw new IllegalArgumentException("사용자 ID는 필수입니다");
			return new CreateWalletResult(null, false);
		}

		if (userWalletRepository.existsByUserId(userId)) {
			// throw new WalletException(ALREADY_EXISTS_WALLET);
			return new CreateWalletResult(null, true);
		}

		UserWallet userwallet = UserWallet.createUserWallet(userId);
		userWalletRepository.save(userwallet); //DataIntegrity exception은 RestControllerAdvice에서 처리
		// log.info(userId.toString());
		return new CreateWalletResult(userwallet.getWalletId(), true);
	}

	@Observed(name = "wallet.use-point")
	@Transactional
	public UseWalletResult usePoint(UseWalletCommand command) {
		StopWatch sw = new StopWatch("usePoint");

		sw.start("load-buyer-wallet");
		UserWallet buyerWallet = userWalletRepository.findByUserId(command.buyerId())
			.orElseThrow(() -> new WalletException(WalletErrorCode.WALLET_NOT_FOUND));
		sw.stop();

		// 같은 idempotency key있을 때 처리 상태에 따라 return
		String idempotencyKey = command.idempotencyKey().toString();
		sw.start("idempotency-check");
		Optional<PointTransactionRequestHistory> existing =
			pointTxRequestHistoryRepository.findByIdempotencyKeyAndRefIdAndPointRequestType(
				idempotencyKey, command.orderId(), PointRequestType.ORDER_PAYMENT
			);
		sw.stop();
		if (existing.isPresent()) {
			PointTransactionRequestHistory history = existing.get();
			if (history.isSuccess()) {
				return UseWalletResult.success(buyerWallet.checkBalance());
			}
			if (history.isInsufficient()) {
				PointShortage pointShortage = history.getPointShortage();
				return UseWalletResult.insufficient(pointShortage.getBalance(), pointShortage.getShortage());
			}
		}

		//다른 결제 요청이지만 이미 포인트 사용 내역이 있다면 종료
		sw.start("duplicate-tx-check");
		boolean duplicate = pointTransactionRepository.existsByRefIdAndPointTxType(
			command.orderId(), PointTxType.BUYER_PAYMENT);
		sw.stop();
		if (duplicate) {
			return UseWalletResult.success(buyerWallet.checkBalance());
		}

		PointTransactionRequestHistory attempt =
			PointTransactionRequestHistory.orderPaymentAttempt(
				buyerWallet, command.orderTotalAmount(), command.orderId(), idempotencyKey
			);

		long currentBalance = buyerWallet.checkBalance();
		if (currentBalance < command.orderTotalAmount()) {
			long shortage = command.orderTotalAmount() - currentBalance;
			attempt.insufficient();
			pointTxRequestHistoryRepository.save(attempt);
			return UseWalletResult.insufficient(currentBalance, shortage);
		}

		sw.start("load-escrow-wallet");
		SystemWallet systemEscrow = systemWalletProvider.getEscrowWallet();
		sw.stop();
		UUID escrowWalletId = systemEscrow.getWalletId();

		long orderTotalAmount = command.orderTotalAmount();
		UUID refId = command.orderId();
		PointTransaction userTx = buyerWallet.buyerPayment(orderTotalAmount, refId);
		PointTransaction escrowTx = systemEscrow.recordEscrowDepositPointTx(orderTotalAmount, refId);

		sw.start("save-transactions");
		attempt.success();
		pointTxRequestHistoryRepository.save(attempt);
		userWalletRepository.save(buyerWallet);
		pointTransactionRepository.saveAll(List.of(userTx, escrowTx));

		SystemWalletOutbox outbox = SystemWalletOutbox.create(escrowWalletId, +orderTotalAmount,
			PointTxType.ESCROW_DEPOSIT, refId, RefType.ORDER);
		systemWalletOutboxRepository.save(outbox);
		sw.stop();

		applicationEventPublisher.publishEvent(SystemWalletOutboxEvent.of(outbox.getOutboxId(), escrowWalletId));

		log.info("[usePoint] {}", sw.prettyPrint());
		return UseWalletResult.success(buyerWallet.checkBalance());
	}

	@Observed(name = "wallet.transfer-for-settlement")
	@Override
	@Transactional(propagation = Propagation.MANDATORY) // 부모 트랜잭션(정산)에 반드시 합류하도록 설정
	public void transferForSettlement(UUID orderId, UUID sellerId, long orderTotalAmount, long sellerAmount, long feeAmount) { // dto로 변경 예정
		SystemWallet escrowWallet = systemWalletProvider.getEscrowWallet();
		SystemWallet feeWallet = systemWalletProvider.getFeeWallet();
		UserWallet sellerWallet = userWalletRepository.findByUserId(sellerId)
			.orElseThrow(() -> new WalletException(WALLET_NOT_FOUND));

		List<PointTransaction> transactions = new ArrayList<>();
		System.out.println(orderTotalAmount);
		transactions.add(escrowWallet.recordSettleOutTx(orderTotalAmount, orderId));

		if (sellerAmount > 0) {
			transactions.add(sellerWallet.settleIn(sellerAmount, orderId));
			userWalletRepository.save(sellerWallet);
		}

		if (feeAmount > 0) {
			transactions.add(feeWallet.recordIncreaseFeeRevenueTx(feeAmount, orderId));
		}

		if (!transactions.isEmpty()) {
			pointTransactionRepository.saveAll(transactions);
		}

		UUID escrowWalletId = escrowWallet.getWalletId();
		UUID feeWalletId = feeWallet.getWalletId();

		SystemWalletOutbox escrowOutbox = SystemWalletOutbox.create(
			 escrowWalletId, -orderTotalAmount, PointTxType.SETTLEMENT_OUT, orderId, RefType.ORDER
		);
		SystemWalletOutbox feeOutbox = SystemWalletOutbox.create(
			feeWalletId, +feeAmount, PointTxType.FEE_REVENUE, orderId, RefType.ORDER
		);
		systemWalletOutboxRepository.save(escrowOutbox);
		systemWalletOutboxRepository.save(feeOutbox);

		applicationEventPublisher.publishEvent(SystemWalletOutboxEvent.of(escrowOutbox.getOutboxId(), escrowWalletId));
		applicationEventPublisher.publishEvent(SystemWalletOutboxEvent.of(feeOutbox.getOutboxId(), feeWalletId));

	}

	@Observed(name = "wallet.charge-point")
	public ChargePointResult chargePoint(ChargePointCommand command) {
		StopWatch sw = new StopWatch("chargePoint");
		String idempotencyKey = command.idempotencyKey();

		sw.start("idempotency-check");
		Optional<PointTransactionRequestHistory> pointTxRequestHistory = idempotencyHandler.check(idempotencyKey);
		sw.stop();
		if (pointTxRequestHistory.isPresent()) {
			throw new IllegalArgumentException("이미 요청된 충전입니다.");
		}

		sw.start("save-charge-request");
		UUID historyId = pointTxRequestService.chargePointRequest(command);
		sw.stop();

		sw.start("payment-feign");
		ChargePointResult result = paymentPort.chargePoint(command.userId(), historyId, command.chargeAmount());
		sw.stop();

		log.info("[chargePoint] {}", sw.prettyPrint());
		return result;
	}

	@Observed(name = "wallet.charge-complete")
	@Transactional
	public void chargeComplete(ChargeCompleteCommand command) {
		UserWallet userWallet = userWalletRepository.findByUserId(command.userId())
				.orElseThrow(() -> new WalletException(WALLET_NOT_FOUND));
		SystemWallet systemPointSourceWallet = systemWalletProvider.getPointSourceWallet();

		long chargeAmount = command.chargedAmount();
		UUID refId = command.paymentId();

		PointTransactionRequestHistory pointTxRequestHistory =
				pointTxRequestHistoryRepository.findById(command.pointTxRequestHistoryId())
						.orElseThrow(() -> new WalletException(WALLET_POINT_TX_REQUEST_NOT_FOUND));

		if(PointRequestStatus.SUCCESS == command.requestResultStatus()) {
			pointTxRequestHistory.success();
			UUID systemPointSourceWalletId = systemPointSourceWallet.getWalletId();
			PointTransaction chargeTx = userWallet.chargeComplete(chargeAmount, refId);
			PointTransaction pointSourceTx = systemPointSourceWallet.recordDecreasePointSourceTx(chargeAmount, refId);

			userWalletRepository.save(userWallet);
			pointTransactionRepository.save(chargeTx);

			pointTransactionRepository.save(pointSourceTx);
			SystemWalletOutbox outbox = SystemWalletOutbox.create(systemPointSourceWalletId , -chargeAmount,
				PointTxType.POINT_SOURCE_OUT, refId, RefType.PAYMENT);
			systemWalletOutboxRepository.save(outbox);

			applicationEventPublisher.publishEvent(SystemWalletOutboxEvent.of(outbox.getOutboxId(), systemPointSourceWalletId));
		} else {
			pointTxRequestHistory.fail();
		}

		pointTxRequestHistoryRepository.save(pointTxRequestHistory);
	}

	@Observed(name = "wallet.withdraw-point")
	public WithdrawPointResult withdrawPoint(WithdrawPointCommand command) {
		StopWatch sw = new StopWatch("withdrawPoint");
		String idempotencyKey = command.idempotencyKey();

		sw.start("idempotency-check");
		Optional<PointTransactionRequestHistory> pointTxRequestHistory = idempotencyHandler.check(idempotencyKey);
		sw.stop();
		if (pointTxRequestHistory.isPresent()) {
			return new WithdrawPointResult(pointTxRequestHistory.get().getPointTxRequestHistoryId());
		}

		sw.start("save-withdraw-request");
		UUID historyId;
		try {
			historyId = pointTxRequestService.withdrawPointRequest(command);
		} catch (Exception e) {
			sw.stop();
			log.error("[withdrawPoint] withdrawPointRequest failed userId={} amount={}", command.userId(), command.withdrawAmount(), e);
			throw e;
		}
		sw.stop();

		sw.start("payment-feign");
		try {
			paymentPort.withdrawPoint(command.userId(), historyId, command.withdrawAmount());
		} catch (Exception e) {
			sw.stop();
			log.error("[withdrawPoint] paymentPort failed userId={} historyId={}", command.userId(), historyId, e);
			throw e;
		}
		sw.stop();

		log.info("[withdrawPoint] {}", sw.prettyPrint());
		return new WithdrawPointResult(historyId);
	}

	@Observed(name = "wallet.withdraw-complete")
	@Transactional
	public void withdrawComplete(WithdrawCompleteCommand command) {
		UserWallet userWallet = userWalletRepository.findByUserId(command.userId())
			.orElseThrow(() -> new WalletException(WALLET_NOT_FOUND));
		SystemWallet systemPointSourceWallet = systemWalletProvider.getPointSourceWallet();

		PointTransactionRequestHistory pointTxRequestHistory =
			pointTxRequestHistoryRepository.findById(command.pointTxRequestHistoryId())
				.orElseThrow(() -> new WalletException(WALLET_POINT_TX_REQUEST_NOT_FOUND));

		long withdrawAmount = command.withdrawAmount();
		UUID refId = command.paymentId();

		if(PointRequestStatus.SUCCESS == command.requestResultStatus()) {
			pointTxRequestHistory.success();
			UUID pointSourceWalletId = systemPointSourceWallet.getWalletId();
			long requestedAmount = pointTxRequestHistory.getRequestPoint();

			PointTransaction withdrawTx = userWallet.withdraw(requestedAmount, withdrawAmount, refId);
			PointTransaction pointSourceTx = systemPointSourceWallet.recordIncreasePointSourceTx(withdrawAmount, refId);

			userWalletRepository.save(userWallet);
			pointTransactionRepository.save(withdrawTx);

			pointTransactionRepository.save(pointSourceTx);
			SystemWalletOutbox outbox = SystemWalletOutbox.create(pointSourceWalletId, withdrawAmount,
				PointTxType.POINT_SOURCE_IN, refId, RefType.PAYMENT);
			systemWalletOutboxRepository.save(outbox);

			applicationEventPublisher.publishEvent(SystemWalletOutboxEvent.of(outbox.getOutboxId(), pointSourceWalletId));
		} else {
			pointTxRequestHistory.fail();
		}

		pointTxRequestHistoryRepository.save(pointTxRequestHistory);
	}
}
