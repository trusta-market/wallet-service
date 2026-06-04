package com.trustamarket.walletservice.wallet.application.command;

import static com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode.*;
import static java.lang.Math.*;

import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trustamarket.walletservice.wallet.application.dto.event.CancelCompletedEvent;
import com.trustamarket.walletservice.wallet.application.dto.event.SystemWalletOutboxEvent;
import com.trustamarket.walletservice.wallet.application.dto.message.CancelMessage;
import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;
import com.trustamarket.walletservice.wallet.domain.entity.SystemWallet;
import com.trustamarket.walletservice.wallet.domain.entity.SystemWalletOutbox;
import com.trustamarket.walletservice.wallet.domain.entity.UserWallet;
import com.trustamarket.walletservice.wallet.domain.enums.PointTxType;
import com.trustamarket.walletservice.wallet.domain.enums.RefType;
import com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRepository;
import com.trustamarket.walletservice.wallet.domain.repository.SystemWalletOutboxRepository;
import com.trustamarket.walletservice.wallet.domain.repository.UserWalletRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletMessageService implements WalletMessageUsecase {

	private final UserWalletRepository userWalletRepository;
	private final SystemWalletOutboxRepository systemWalletOutboxRepository;
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


		UserWallet userWallet= userWalletRepository.findByUserId(buyerId).orElseThrow(
			() -> new WalletException(WALLET_NOT_FOUND)
		);

		PointTransaction pointTransaction = pointTransactionRepository.
			findByOrderIdAndWalletIdAndPointTxType(orderId, userWallet.getWalletId(), PointTxType.BUYER_PAYMENT)
			.orElseThrow(() -> new WalletException(WalletErrorCode.WALLET_POINT_TX_NOT_FOUND));

		if (abs(pointTransaction.getChangeAmount()) != cancelledAmount) {
			throw new WalletException(CANCELLED_AMOUNT_NOT_MATCH);
		}

		SystemWallet escrowWallet = systemWalletProvider.getEscrowWallet();
		PointTransaction pointUserCancelTx = userWallet.cancelIn(cancelledAmount, message.orderId());
		PointTransaction pointEscrowCancelTx = escrowWallet.recordCancelOutTx(
			cancelledAmount, orderId);

		userWalletRepository.save(userWallet);
		pointTransactionRepository.saveAll(List.of(pointUserCancelTx, pointEscrowCancelTx));

		SystemWalletOutbox outbox= SystemWalletOutbox.create(escrowWallet.getWalletId(), -cancelledAmount, PointTxType.CANCEL_OUT, orderId, RefType.ORDER);
		systemWalletOutboxRepository.save(outbox);
		eventPublisher.publishEvent(SystemWalletOutboxEvent.of(outbox.getOutboxId(), escrowWallet.getWalletId()));

		CancelCompletedEvent cancelCompletedEvent = CancelCompletedEvent.of(orderId, cancelledAmount);
		eventPublisher.publishEvent(cancelCompletedEvent);
	}
}