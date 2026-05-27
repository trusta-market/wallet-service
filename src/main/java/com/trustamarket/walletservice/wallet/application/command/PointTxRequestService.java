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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointTxRequestService {
	private final UserWalletRepository userWalletRepository;
	private final PointTransactionRequestHistoryRepository pointTxRequestHistoryRepository;

	@Transactional // find와 save간의 transaction
	public UUID withdrawPointRequest(WithdrawPointCommand command) {
		UserWallet userWallet = userWalletRepository.findByUserId(command.userId())
			.orElseThrow(() -> {
				log.error(String.valueOf(command.userId()));
				return new WalletException(WalletErrorCode.WALLET_NOT_FOUND);
			});

		if(!userWallet.isEnough(command.withdrawAmount())) {
			throw new WalletException(WalletErrorCode.INVALID_BALANCE);
		}

		PointTransactionRequestHistory pointTxRequestHistory = pointTxRequestHistoryRepository.save(
			PointTransactionRequestHistory.payoutRequest(userWallet, command.withdrawAmount(), command.idempotencyKey())
		);

		return pointTxRequestHistory.getPointTxRequestHistoryId();
	}

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
