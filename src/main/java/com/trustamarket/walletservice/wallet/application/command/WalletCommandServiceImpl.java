package com.trustamarket.walletservice.wallet.application.command;

import com.trustamarket.walletservice.wallet.application.dto.command.*;
import com.trustamarket.walletservice.wallet.application.dto.result.ChargePointResult;
import com.trustamarket.walletservice.wallet.application.dto.result.CreateWalletResult;
import com.trustamarket.walletservice.wallet.application.dto.result.UseWalletResult;
import com.trustamarket.walletservice.wallet.application.dto.result.WithdrawPointResult;
import com.trustamarket.walletservice.wallet.application.port.PaymentPort;
import com.trustamarket.walletservice.wallet.domain.entity.*;
import com.trustamarket.walletservice.wallet.domain.enums.PointRequestStatus;
import com.trustamarket.walletservice.wallet.domain.enums.PointRequestType;
import com.trustamarket.walletservice.wallet.domain.enums.PointTxType;
import com.trustamarket.walletservice.wallet.domain.enums.RefType;
import com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRepository;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRequestHistoryRepository;
import com.trustamarket.walletservice.wallet.domain.repository.SystemWalletRepository;
import com.trustamarket.walletservice.wallet.domain.repository.UserWalletRepository;
import com.trustamarket.walletservice.wallet.global.handler.IdempotencyHandler;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode.WALLET_NOT_FOUND;
import static com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode.WALLET_POINT_TX_REQUEST_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletCommandServiceImpl implements WalletCommandService {

	private final UserWalletRepository userWalletRepository;
	private final SystemWalletRepository systemWalletRepository;
	private final SystemWalletProvider systemWalletProvider;
	private final PaymentPort paymentPort;

	private final PointTransactionRepository pointTransactionRepository;
	private final PointTransactionRequestHistoryRepository pointTxRequestHistoryRepository;

	private final PointTxRequestService pointTxRequestService;
	private final IdempotencyHandler idempotencyHandler;

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
		log.info(userId.toString());
		return new CreateWalletResult(userwallet.getWalletId(), true);
	}

	@Observed(name = "wallet.use-point")
	@Transactional
	public UseWalletResult usePoint(UseWalletCommand command) {
		UserWallet buyerWallet = userWalletRepository.findByUserId(command.buyerId())
			.orElseThrow(() -> new WalletException(WalletErrorCode.WALLET_NOT_FOUND));

		// 같은 idempotency key있을 때 처리 상태에 따라 return
		String idempotencyKey = command.idempotencyKey().toString();
		Optional<PointTransactionRequestHistory> existing =
			pointTxRequestHistoryRepository.findByIdempotencyKeyAndRefIdAndPointRequestType(
				idempotencyKey, command.orderId(), PointRequestType.ORDER_PAYMENT
			);
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
		if (pointTransactionRepository.existsByRefIdAndPointTxType(
			command.orderId(), PointTxType.BUYER_PAYMENT)) {
			return UseWalletResult.success(buyerWallet.checkBalance());
		}


		PointTransactionRequestHistory attempt =
			PointTransactionRequestHistory.orderPaymentAttempt(
				buyerWallet, command.totalAmount(), command.orderId(), idempotencyKey
			);

		long currentBalance = buyerWallet.checkBalance();
		if (currentBalance < command.totalAmount()) {
			long shortage = command.totalAmount() - currentBalance;
			attempt.insufficient();
			pointTxRequestHistoryRepository.save(attempt);
			return UseWalletResult.insufficient(currentBalance, shortage);
		}

		PointTransaction userTx = buyerWallet.decrease(
			command.totalAmount(), command.orderId(), RefType.ORDER, PointTxType.BUYER_PAYMENT
		);
		userWalletRepository.save(buyerWallet);

		//system wallet은 원자적 update로 save 필요 없음
		UUID systemEscrowId = systemWalletProvider.getEscrowWalletId();
		long afterBalance = systemWalletRepository.increaseBalance(systemEscrowId, command.totalAmount());
		long escrowBalanceBefore = afterBalance - command.totalAmount();

		PointTransaction escrowTx = PointTransaction.create(
				systemEscrowId, escrowBalanceBefore, command.totalAmount(),
			PointTxType.ESCROW_DEPOSIT, command.orderId(), RefType.ORDER
		);

		attempt.success();
		pointTxRequestHistoryRepository.save(attempt);
		pointTransactionRepository.saveAll(List.of(userTx, escrowTx));

		return UseWalletResult.success(buyerWallet.checkBalance());
	}

	@Observed(name = "wallet.transfer-for-settlement")
	@Override
	@Transactional(propagation = Propagation.MANDATORY) // 부모 트랜잭션(정산)에 반드시 합류하도록 설정
	public void transferForSettlement(UUID orderId, UUID sellerId, long totalAmount, long sellerAmount, long feeAmount) { // dto로 변경 예정

		UserWallet sellerWallet = userWalletRepository.findByUserId(sellerId)
			.orElseThrow(() -> new WalletException(WALLET_NOT_FOUND));

		List<PointTransaction> transactions = new ArrayList<>();
		if (sellerAmount > 0) {
			transactions.add(sellerWallet.settleIn(sellerAmount, orderId));
			userWalletRepository.save(sellerWallet);
		}

		UUID escrowWalletId = systemWalletProvider.getEscrowWalletId();
		Optional<Long> afterBalance = systemWalletRepository.decreaseBalanceIfSufficient(escrowWalletId, totalAmount);
		if (afterBalance.isEmpty()) {
			throw new IllegalArgumentException("정산을 위한 잔액이 충분하지 않습니다.");
		}
		long escrowBalanceBefore = afterBalance.get() + totalAmount;
		transactions.add(PointTransaction.create(escrowWalletId, escrowBalanceBefore, -totalAmount,
			PointTxType.SETTLEMENT_OUT, orderId, RefType.ORDER));

		if (feeAmount > 0) {
			UUID feeWalletId = systemWalletProvider.getFeeWalletId();
			long afterFeeBalance = systemWalletRepository.increaseBalance(feeWalletId, feeAmount);
			long beforeFeeBalance =  afterFeeBalance - feeAmount;
			transactions.add(PointTransaction.create(feeWalletId, beforeFeeBalance, feeAmount,
				PointTxType.FEE_REVENUE, orderId,RefType.ORDER));
		}

		if (!transactions.isEmpty()) {
			pointTransactionRepository.saveAll(transactions);
		}
	}

	@Observed(name = "wallet.charge-point")
	public ChargePointResult chargePoint(ChargePointCommand command) {
		String idempotencyKey = command.idempotencyKey();

		Optional<PointTransactionRequestHistory> pointTxRequestHistory = idempotencyHandler.check(idempotencyKey);
		if (pointTxRequestHistory.isPresent()) {
			throw new IllegalArgumentException("이미 요청된 충전입니다.");
		}

		UUID historyId = pointTxRequestService.chargePointRequest(command);

		ChargePointResult result = paymentPort.chargePoint(
				command.userId(), historyId, command.chargeAmount()
		);

		return result;
	}

	@Observed(name = "wallet.charge-complete")
	@Transactional
	public void chargeComplete(ChargeCompleteCommand command) {
		UserWallet userWallet = userWalletRepository.findByUserId(command.userId())
				.orElseThrow(() -> new WalletException(WALLET_NOT_FOUND));

		long chargeAmount = command.chargedAmount();
		UUID refId = command.paymentId();

		PointTransactionRequestHistory pointTxRequestHistory =
				pointTxRequestHistoryRepository.findById(command.pointTxRequestHistoryId())
						.orElseThrow(() -> new WalletException(WALLET_POINT_TX_REQUEST_NOT_FOUND));

		if(PointRequestStatus.SUCCESS == command.requestResultStatus()) {
			pointTxRequestHistory.success();
			PointTransaction chargeTx = userWallet.chargeComplete(chargeAmount, refId);

			userWalletRepository.save(userWallet);
			pointTxRequestHistoryRepository.save(pointTxRequestHistory);
			pointTransactionRepository.save(chargeTx);

			UUID systemPointSourceWalletId = systemWalletProvider.getPointSourceWalletId();
			long pointSourceBalanceAfter = systemWalletRepository.decreaseBalanceUnchecked(systemPointSourceWalletId, chargeAmount); // 이 이후로는 DB row락 걸림 (update했으니까)
			long pointSourceBalanceBefore = pointSourceBalanceAfter + chargeAmount;
			PointTransaction pointSourceTx = PointTransaction.create(systemPointSourceWalletId, pointSourceBalanceBefore, -chargeAmount,
				PointTxType.POINT_SOURCE_OUT, refId, RefType.PAYMENT);
			pointTransactionRepository.save(pointSourceTx);
		} else {
			pointTxRequestHistory.fail();
			pointTxRequestHistoryRepository.save(pointTxRequestHistory);
		}
	}

	@Observed(name = "wallet.withdraw-point")
	public WithdrawPointResult withdrawPoint(WithdrawPointCommand command) {
		String idempotencyKey = command.idempotencyKey();

		Optional<PointTransactionRequestHistory> pointTxRequestHistory = idempotencyHandler.check(idempotencyKey);
		if (pointTxRequestHistory.isPresent()) {
			return new WithdrawPointResult(pointTxRequestHistory.get().getPointTxRequestHistoryId());
		}

		//트렌젝션 나눴기 때문에 paymentPort에 대한 saga 힘들다.
		UUID historyId = pointTxRequestService.withdrawPointRequest(command);

		paymentPort.withdrawPoint(
			command.userId(), historyId, command.withdrawAmount()
		); // 이미 Reqhistory저장했는데 여기서 오류가 난다면 문제가 됨.

		return new WithdrawPointResult(historyId);
	}

	@Observed(name = "wallet.withdraw-complete")
	@Transactional
	public void withdrawComplete(WithdrawCompleteCommand command) {
		UserWallet userWallet = userWalletRepository.findByUserId(command.userId())
			.orElseThrow(() -> new WalletException(WALLET_NOT_FOUND));

		PointTransactionRequestHistory pointTxRequestHistory =
			pointTxRequestHistoryRepository.findById(command.pointTxRequestHistoryId())
				.orElseThrow(() -> new WalletException(WALLET_POINT_TX_REQUEST_NOT_FOUND));

		long withdrawAmount = command.withdrawAmount();
		UUID refId = command.paymentId();

		if(PointRequestStatus.SUCCESS == command.requestResultStatus()) {
			pointTxRequestHistory.success();
			pointTxRequestHistoryRepository.save(pointTxRequestHistory);

			long requestedAmount = pointTxRequestHistory.getRequestPoint();
			PointTransaction withdrawTx = userWallet.withdraw(requestedAmount, withdrawAmount, refId); // 출금된 금액과 요청한 포인트 다른지 확인하고 있음
			userWalletRepository.save(userWallet);

			UUID systemPointSourceWalletId = systemWalletProvider.getPointSourceWalletId();
			long afterBalance = systemWalletRepository.increaseBalance(systemPointSourceWalletId, withdrawAmount);
			long beforeWithdraw = afterBalance - requestedAmount;
			PointTransaction pointSourceTx = PointTransaction.create(systemPointSourceWalletId, beforeWithdraw, withdrawAmount,
				PointTxType.POINT_SOURCE_IN, refId, RefType.PAYMENT);
			pointTransactionRepository.save(withdrawTx);
			pointTransactionRepository.save(pointSourceTx);
		} else {
			pointTxRequestHistory.fail();
			pointTxRequestHistoryRepository.save(pointTxRequestHistory);
		}
	}
}
