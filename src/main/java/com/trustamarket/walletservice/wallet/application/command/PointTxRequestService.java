package com.trustamarket.walletservice.wallet.application.command;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trustamarket.walletservice.wallet.application.dto.command.ChargePointCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.WithdrawPointCommand;
import com.trustamarket.walletservice.wallet.domain.entity.PointTransactionRequestHistory;
import com.trustamarket.walletservice.wallet.domain.entity.UserWallet;
import com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRequestHistoryRepository;
import com.trustamarket.walletservice.wallet.domain.repository.UserWalletRepository;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointTxRequestService {
	private final UserWalletRepository userWalletRepository;
	private final PointTransactionRequestHistoryRepository pointTxRequestHistoryRepository;

	@Observed(name = "wallet.withdraw-request-save")
	@Transactional // find와 save간의 transaction
	public UUID withdrawPointRequest(WithdrawPointCommand command) {
		log.info("[withdrawPointRequest] userId={} amount={}", command.userId(), command.withdrawAmount());

		UserWallet userWallet = userWalletRepository.findByUserId(command.userId())
			.orElseThrow(() -> {
				log.error("[withdrawPointRequest] wallet not found userId={}", command.userId());
				return new WalletException(WalletErrorCode.WALLET_NOT_FOUND);
			});

		log.info("[withdrawPointRequest] balance={} requested={}", userWallet.checkBalance(), command.withdrawAmount());

		if(!userWallet.isEnough(command.withdrawAmount())) {
			log.warn("[withdrawPointRequest] insufficient balance userId={} balance={} requested={}",
				command.userId(), userWallet.checkBalance(), command.withdrawAmount());
			throw new WalletException(WalletErrorCode.INVALID_BALANCE);
		}

		PointTransactionRequestHistory pointTxRequestHistory = pointTxRequestHistoryRepository.save(
			PointTransactionRequestHistory.payoutRequest(userWallet, command.withdrawAmount(), command.idempotencyKey())
		);

		log.info("[withdrawPointRequest] saved historyId={}", pointTxRequestHistory.getPointTxRequestHistoryId());
		return pointTxRequestHistory.getPointTxRequestHistoryId();
	}

	@Observed(name = "wallet.charge-request-save")
	@Transactional
	public UUID chargePointRequest(ChargePointCommand command) {
		UserWallet userWallet = userWalletRepository.findByUserId(command.userId())
				.orElseThrow(() -> new WalletException(WalletErrorCode.WALLET_NOT_FOUND));

		PointTransactionRequestHistory pointTxRequestHistory = pointTxRequestHistoryRepository.save(
				PointTransactionRequestHistory.paymentRequest(userWallet, command.chargeAmount(), command.idempotencyKey())
		);

		return pointTxRequestHistory.getPointTxRequestHistoryId();
	}
}
