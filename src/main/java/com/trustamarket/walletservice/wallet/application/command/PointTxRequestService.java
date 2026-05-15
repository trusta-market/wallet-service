package com.trustamarket.walletservice.wallet.application.command;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.application.dto.command.ChargePointCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trustamarket.walletservice.wallet.application.dto.command.WithdrawPointCommand;
import com.trustamarket.walletservice.wallet.domain.entity.PointTransactionRequestHistory;
import com.trustamarket.walletservice.wallet.domain.entity.Wallet;
import com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRequestHistoryRepository;
import com.trustamarket.walletservice.wallet.domain.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointTxRequestService {
	private final WalletRepository walletRepository;
	private final PointTransactionRequestHistoryRepository pointTxRequestHistoryRepository;

	@Transactional // find와 save간의 transaction
	public UUID withdrawPointRequest(WithdrawPointCommand command) {
		Wallet userWallet = walletRepository.findByUserId(command.userId())
			.orElseThrow(() -> new WalletException(WalletErrorCode.WALLET_NOT_FOUND));

		if (userWallet.isNotUser()) {
			throw new WalletException(WalletErrorCode.SYSTEM_WALLET_WITHDRAWAL_NOT_ALLOWED);
		}

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
		Wallet userWallet = walletRepository.findByUserId(command.userId())
				.orElseThrow(() -> new WalletException(WalletErrorCode.WALLET_NOT_FOUND));

		if (userWallet.isNotUser()) {
			throw new WalletException(WalletErrorCode.SYSTEM_WALLET_CHARGE_NOT_ALLOWED);
		}

		PointTransactionRequestHistory pointTxRequestHistory = pointTxRequestHistoryRepository.save(
				PointTransactionRequestHistory.paymentRequest(userWallet, command.chargeAmount())
		);

		return pointTxRequestHistory.getPointTxRequestHistoryId();
	}
}
