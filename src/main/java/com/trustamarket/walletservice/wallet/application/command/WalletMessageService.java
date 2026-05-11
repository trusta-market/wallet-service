package com.trustamarket.walletservice.wallet.application.command;

import static com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode.*;

import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trustamarket.walletservice.wallet.application.dto.event.CancelCompletedEvent;
import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;
import com.trustamarket.walletservice.wallet.domain.entity.Wallet;
import com.trustamarket.walletservice.wallet.domain.enums.PointTxType;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRepository;
import com.trustamarket.walletservice.wallet.domain.repository.WalletRepository;
import com.trustamarket.walletservice.wallet.application.dto.message.CancelMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletMessageService implements WalletMessageUsecase {

	private final WalletRepository walletRepository;
	private final PointTransactionRepository pointTransactionRepository;
	private final SystemWalletProvider systemWalletProvider;
	private final ApplicationEventPublisher eventPublisher;
	@Override
	@Transactional
	public void cancelProcess(CancelMessage message) {
		UUID orderId = message.orderId();
		UUID buyerId = message.buyerId();
		long cancelledAmount = message.cancelledAmount();

		if (pointTransactionRepository.existsByRefIdAndPointTxType(orderId, PointTxType.CANCEL_IN)) {
			//이미 table에 저장은 되었는데 ack를 못 받은거면 -> listener에서 확인
			throw new WalletException(ALREADY_CANCELLED);
		}


		Wallet userWallet= walletRepository.findByUserId(buyerId).orElseThrow(
			() -> new WalletException(WALLET_NOT_FOUND)
		);

		PointTransaction pointTransaction = pointTransactionRepository.
			findByOrderIdAndWalletIdAndPointTxType(orderId, userWallet.getWalletId(), PointTxType.BUYER_PAYMENT);

		if (pointTransaction.getChangeAmount() != cancelledAmount) {
			throw new WalletException(CANCELLED_AMOUNT_NOT_MATCH);
		}

		Wallet escrowWallet = systemWalletProvider.getEscrowWallet();
		PointTransaction pointUserCancelTx = userWallet.cancelIn(cancelledAmount, message.orderId());
		PointTransaction pointEscrowCancelTx = escrowWallet.cancelOut(
			cancelledAmount, orderId);

		walletRepository.save(userWallet);
		walletRepository.save(escrowWallet);
		pointTransactionRepository.saveAll(List.of(pointUserCancelTx, pointEscrowCancelTx));

		CancelCompletedEvent cancelCompletedEvent = CancelCompletedEvent.of(orderId, cancelledAmount);

		eventPublisher.publishEvent(cancelCompletedEvent);
	}
}