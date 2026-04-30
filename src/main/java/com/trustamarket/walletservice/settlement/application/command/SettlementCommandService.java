package com.trustamarket.walletservice.settlement.application.command;

import static com.trustamarket.walletservice.settlement.domain.exception.SettlementErrorCode.*;
import static com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trustamarket.walletservice.settlement.application.dto.message.SettlePointSettlementMessage;
import com.trustamarket.walletservice.settlement.domain.entity.SettlementHistory;
import com.trustamarket.walletservice.settlement.domain.exception.SettlementException;
import com.trustamarket.walletservice.settlement.domain.fee.FeeCalculation;
import com.trustamarket.walletservice.settlement.domain.repository.SettlementHistoryRepository;
import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;
import com.trustamarket.walletservice.wallet.domain.entity.Wallet;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRepository;
import com.trustamarket.walletservice.wallet.domain.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettlementCommandService implements SettlementCommandUsecase {

	private final WalletRepository walletRepository;
	private final PointTransactionRepository pointTransactionRepository;
	private final SettlementHistoryRepository settlementHistoryRepository;
	private final FeeCalculator feeCalculator;
	private final SystemWalletProvider systemWalletProvider;

	@Override
	@Transactional
	public void process(SettlePointSettlementMessage message) {
		//멱등성 고민 중 -> 우선 saveAndFlush + DataIntegrityException으로 진행하려고 했으나 unique 제약만 선별하는데 문제 발생
		if (settlementHistoryRepository.existsByEventId(message.eventId())) {
			//이미 table에 저장은 되었는데 ack를 못 받은거면?
			throw new SettlementException(ALREADY_SETTLED);
		}

		// 수수료 계산
		FeeCalculation fee = feeCalculator.calculate(
			message.totalAmount(),
			message.sellerId()
		);
		fee.verifyMatches(message.totalAmount());

		// 지갑 조회
		Wallet adminWallet = systemWalletProvider.getEscrowWallet();
		Wallet feeWallet = systemWalletProvider.getFeeWallet();
		Wallet sellerWallet = walletRepository.findByUserId(message.sellerId())
			.orElseThrow(() -> new WalletException(WALLET_NOT_FOUND));


		// 포인트 이동
		PointTransaction adminTx = adminWallet.settleOut(
			message.totalAmount(),
			message.orderId()
		);

		PointTransaction sellerTx = sellerWallet.settleIn(
			fee.sellerAmount(),
			message.orderId()
		);

		PointTransaction feeTx = feeWallet.increaseFeeRevenue(
			fee.feeAmount(),
			message.orderId()
		);

		// PointTransaction 저장
		pointTransactionRepository.saveAll(List.of(adminTx, sellerTx, feeTx));

		// 멱등성 이력 저장
		SettlementHistory history = SettlementHistory.complete(
			message.eventId(),
			message.orderId(),
			message.sellerId(),
			message.totalAmount(),
			fee.sellerAmount(),
			fee.feeAmount(),
			fee.appliedRate()
		);
		settlementHistoryRepository.save(history);
	}
}