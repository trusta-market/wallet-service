package com.trustamarket.walletservice.wallet.application.command;

import static com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.observation.annotation.Observed;

import com.trustamarket.walletservice.wallet.application.dto.command.ChargeCompleteCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.ChargePointCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.UseWalletCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.WithdrawCompleteCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.WithdrawPointCommand;
import com.trustamarket.walletservice.wallet.application.dto.result.ChargePointResult;
import com.trustamarket.walletservice.wallet.application.dto.result.CreateWalletResult;
import com.trustamarket.walletservice.wallet.application.dto.result.UseWalletResult;
import com.trustamarket.walletservice.wallet.application.dto.result.WithdrawPointResult;
import com.trustamarket.walletservice.wallet.application.port.PaymentPort;
import com.trustamarket.walletservice.wallet.domain.entity.PointShortage;
import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;
import com.trustamarket.walletservice.wallet.domain.entity.PointTransactionRequestHistory;
import com.trustamarket.walletservice.wallet.domain.entity.SystemWallet;
import com.trustamarket.walletservice.wallet.domain.entity.UserWallet;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
		SystemWallet systemEscrow = systemWalletProvider.getEscrowWallet(); // pre-SELECT (walletId)
		systemWalletRepository.increaseBalance(systemEscrow.getWalletId(), command.totalAmount());
		SystemWallet updatedEscrow = systemWalletProvider.getEscrowWallet();         // post-SELECT (balanceAfter)
		long escrowBalanceBefore = updatedEscrow.checkBalance() - command.totalAmount();

		PointTransaction escrowTx = PointTransaction.create(
			systemEscrow.getWalletId(), escrowBalanceBefore, command.totalAmount(),
			PointTxType.ESCROW_DEPOSIT, command.orderId(), RefType.ORDER
		);

		attempt.success();
		pointTxRequestHistoryRepository.save(attempt);
		userWalletRepository.save(buyerWallet);
		systemWalletRepository.save(systemEscrow);
		pointTransactionRepository.saveAll(List.of(userTx, escrowTx));

		return UseWalletResult.success(buyerWallet.checkBalance());
	}

	@Observed(name = "wallet.transfer-for-settlement")
	@Override
	@Transactional(propagation = Propagation.MANDATORY) // 부모 트랜잭션(정산)에 반드시 합류하도록 설정
	public void transferForSettlement(UUID orderId, UUID sellerId, long totalAmount, long sellerAmount, long feeAmount) { // dto로 변경 예정

		SystemWallet escrowWallet = systemWalletProvider.getEscrowWallet();
		SystemWallet feeWallet = systemWalletProvider.getFeeWallet();
		UserWallet sellerWallet = userWalletRepository.findByUserId(sellerId)
			.orElseThrow(() -> new WalletException(WALLET_NOT_FOUND));

		List<PointTransaction> transactions = new ArrayList<>();
		if (sellerAmount > 0) {
			transactions.add(sellerWallet.settleIn(sellerAmount, orderId));
			userWalletRepository.save(sellerWallet);
		}

		int affected = systemWalletRepository.decreaseBalanceIfSufficient(escrowWallet.getWalletId(), totalAmount);
		if (affected == 0) {
			throw new IllegalArgumentException("정산을 위한 잔액이 충분하지 않습니다.");
		}
		SystemWallet updatedEscrow = systemWalletProvider.getEscrowWallet();
		long escrowBalanceBefore = updatedEscrow.checkBalance() + totalAmount;
		transactions.add(PointTransaction.create(escrowWallet.getWalletId(), escrowBalanceBefore, -totalAmount,
			PointTxType.SETTLEMENT_OUT, orderId, RefType.ORDER));
		System.out.println(totalAmount);



		if (feeAmount > 0) {
			systemWalletRepository.increaseBalance(feeWallet.getWalletId(), feeAmount);
			SystemWallet updatedFee = systemWalletProvider.getFeeWallet();
			long feeBalanceBefore =  updatedFee.checkBalance() - feeAmount;
			transactions.add(PointTransaction.create(feeWallet.getWalletId(), feeBalanceBefore, feeAmount,
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

			//DB에서 로드한 객체는 clearAutomatically로 인해 1차 캐시에서 지워지기 전 save
			userWalletRepository.save(userWallet);
			pointTxRequestHistoryRepository.save(pointTxRequestHistory);

			SystemWallet systemPointSourceWallet = systemWalletProvider.getPointSourceWallet();
			systemWalletRepository.decreaseBalanceUnchecked(systemPointSourceWallet.getWalletId(), chargeAmount); // 이 이후로는 DB row락 걸림 (update했으니까)
			SystemWallet updatedPointSource = systemWalletProvider.getPointSourceWallet();
			long pointSourceBalanceBefore = updatedPointSource.checkBalance() + chargeAmount;
			PointTransaction pointSourceTx = PointTransaction.create(systemPointSourceWallet.getWalletId(), pointSourceBalanceBefore, -chargeAmount,
				PointTxType.POINT_SOURCE_OUT, refId, RefType.PAYMENT);
			pointTransactionRepository.save(chargeTx);
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

			SystemWallet systemPointSourceWallet = systemWalletProvider.getPointSourceWallet();
			systemWalletRepository.increaseBalance(systemPointSourceWallet.getWalletId(), withdrawAmount);
			SystemWallet updatedSystemPointSourceWallet = systemWalletProvider.getPointSourceWallet();
			long beforeWithdraw = updatedSystemPointSourceWallet.checkBalance() - requestedAmount;
			PointTransaction pointSourceTx = PointTransaction.create(systemPointSourceWallet.getWalletId(), beforeWithdraw, withdrawAmount,
				PointTxType.POINT_SOURCE_IN, refId, RefType.PAYMENT);
			pointTransactionRepository.save(withdrawTx);
			pointTransactionRepository.save(pointSourceTx);
		} else {
			pointTxRequestHistory.fail();
			pointTxRequestHistoryRepository.save(pointTxRequestHistory);
		}
	}
}
