package com.trustamarket.walletservice.wallet.application.command;

import static com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;
import com.trustamarket.walletservice.wallet.domain.entity.PointTransactionRequestHistory;
import com.trustamarket.walletservice.wallet.domain.entity.Wallet;
import com.trustamarket.walletservice.wallet.domain.enums.PointRequestStatus;
import com.trustamarket.walletservice.wallet.domain.enums.PointTxType;
import com.trustamarket.walletservice.wallet.domain.enums.RefType;
import com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRepository;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRequestHistoryRepository;
import com.trustamarket.walletservice.wallet.domain.repository.WalletRepository;
import com.trustamarket.walletservice.wallet.global.handler.IdempotencyHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletCommandServiceImpl implements WalletCommandService {

	private final WalletRepository walletRepository;
	private final SystemWalletProvider systemWalletProvider;
	private final PaymentPort paymentPort;

	private final PointTransactionRepository pointTransactionRepository;
	private final PointTransactionRequestHistoryRepository pointTxRequestHistoryRepository;

	private final PointTxRequestService pointTxRequestService;
	private final IdempotencyHandler idempotencyHandler;

	@Transactional
	public CreateWalletResult createWallet(UUID userId) {
		if (userId == null) {
			// throw new IllegalArgumentException("사용자 ID는 필수입니다");
			return new CreateWalletResult(null, false);
		}

		if (walletRepository.existsByUserId(userId)) {
			// throw new WalletException(ALREADY_EXISTS_WALLET);
			return new CreateWalletResult(null, true);
		}

		Wallet wallet = Wallet.createUserWallet(userId);
		walletRepository.save(wallet); //DataIntegrity exception은 RestControllerAdvice에서 처리
		log.info(userId.toString());
		return new CreateWalletResult(wallet.getWalletId(), true);
	}

	@Transactional
	public UseWalletResult usePoint(UseWalletCommand command) {
		Wallet buyerWallet = walletRepository.findByUserId(command.buyerId())
			.orElseThrow(() -> new WalletException(WalletErrorCode.WALLET_NOT_FOUND));

		long currentBalance = buyerWallet.checkBalance();
		if (currentBalance < command.totalAmount()) {
			long shortage = command.totalAmount() - currentBalance;
			return UseWalletResult.insufficient(currentBalance, shortage);
		}

		Wallet systemEscrow = systemWalletProvider.getEscrowWallet();

		PointTransaction userTx = buyerWallet.decrease(
			command.totalAmount(), command.orderId(), RefType.ORDER, PointTxType.BUYER_PAYMENT
		);
		PointTransaction escrowTx = systemEscrow.increase(
			command.totalAmount(), command.orderId(), RefType.ORDER, PointTxType.ESCROW_DEPOSIT
		);

		walletRepository.save(buyerWallet);
		walletRepository.save(systemEscrow);
		pointTransactionRepository.saveAll(List.of(userTx, escrowTx));

		return UseWalletResult.success(buyerWallet.checkBalance());
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY) // 부모 트랜잭션(정산)에 반드시 합류하도록 설정
	public void transferForSettlement(UUID orderId, UUID sellerId, long totalAmount, long sellerAmount, long feeAmount) { // dto로 변경 예정

		Wallet adminWallet = systemWalletProvider.getEscrowWallet();
		Wallet feeWallet = systemWalletProvider.getFeeWallet();
		Wallet sellerWallet = walletRepository.findByUserId(sellerId)
			.orElseThrow(() -> new WalletException(WALLET_NOT_FOUND));

		List<PointTransaction> transactions = new ArrayList<>();
		System.out.println(totalAmount);
		transactions.add(adminWallet.settleOut(totalAmount, orderId));

		if (sellerAmount > 0) {
			transactions.add(sellerWallet.settleIn(sellerAmount, orderId));
		}

		if (feeAmount > 0) {
			transactions.add(feeWallet.increaseFeeRevenue(feeAmount, orderId));
		}

		if (!transactions.isEmpty()) {
			pointTransactionRepository.saveAll(transactions);
		}
	}

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

	private boolean isExistPointTxRequestHistory(UUID requestedHistoryId){
		return pointTxRequestHistoryRepository.existsById(requestedHistoryId);
	}

	@Transactional
	public void chargeComplete(ChargeCompleteCommand command) {
		Wallet userWallet = walletRepository.findByUserId(command.userId())
				.orElseThrow(() -> new WalletException(WALLET_NOT_FOUND));
		Wallet systemPointSourceWallet = systemWalletProvider.getPointSourceWallet();

		long chargeAmount = command.chargedAmount();
		UUID refId = command.paymentId();

		PointTransactionRequestHistory pointTxRequestHistory =
				pointTxRequestHistoryRepository.findById(command.pointTxRequestHistoryId())
						.orElseThrow(() -> new WalletException(WALLET_POINT_TX_REQUEST_NOT_FOUND));

		if(PointRequestStatus.SUCCESS == command.requestResultStatus()) {
			pointTxRequestHistory.success();
			PointTransaction chargeTx = userWallet.chargeComplete(chargeAmount, refId);
			PointTransaction pointSourceTx = systemPointSourceWallet.decreasePointSource(chargeAmount, refId);

			walletRepository.save(userWallet);
			walletRepository.save(systemPointSourceWallet);
			pointTransactionRepository.save(pointSourceTx);
			pointTransactionRepository.save(chargeTx);
		} else {
			pointTxRequestHistory.fail();
		}

		pointTxRequestHistoryRepository.save(pointTxRequestHistory);
	}

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

	@Transactional
	public void withdrawComplete(WithdrawCompleteCommand command) {
		Wallet userWallet = walletRepository.findByUserId(command.userId())
			.orElseThrow(() -> new WalletException(WALLET_NOT_FOUND));
		Wallet systemPointSourceWallet = systemWalletProvider.getPointSourceWallet();

		PointTransactionRequestHistory pointTxRequestHistory =
			pointTxRequestHistoryRepository.findById(command.pointTxRequestHistoryId())
				.orElseThrow(() -> new WalletException(WALLET_POINT_TX_REQUEST_NOT_FOUND));

		long withdrawAmount = command.withdrawAmount();
		UUID refId = command.paymentId();

		if(PointRequestStatus.SUCCESS == command.requestResultStatus()) {
			pointTxRequestHistory.success();
			long requestedAmount = pointTxRequestHistory.getRequestPoint();
			PointTransaction withdrawTx = userWallet.withdraw(requestedAmount, withdrawAmount, refId);
			PointTransaction pointSourceTx = systemPointSourceWallet.increasePointSource(withdrawAmount, refId);

			walletRepository.save(userWallet);
			walletRepository.save(systemPointSourceWallet);
			pointTransactionRepository.save(withdrawTx);
			pointTransactionRepository.save(pointSourceTx);
		} else {
			pointTxRequestHistory.fail();
		}

		pointTxRequestHistoryRepository.save(pointTxRequestHistory);
	}
}
