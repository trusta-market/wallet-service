package com.trustamarket.walletservice.wallet.application.query;

import static com.trustamarket.walletservice.wallet.domain.enums.PointTxType.*;
import static com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trustamarket.walletservice.wallet.application.dto.query.GetPointTransactionQuery;
import com.trustamarket.walletservice.wallet.application.dto.result.GetPointTransactionPageResult;
import com.trustamarket.walletservice.wallet.application.dto.result.GetPointUsageResult;
import com.trustamarket.walletservice.wallet.domain.entity.PointShortage;
import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;
import com.trustamarket.walletservice.wallet.domain.entity.Wallet;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;
import com.trustamarket.walletservice.wallet.domain.repository.PointShortageRepository;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRepository;
import com.trustamarket.walletservice.wallet.domain.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class WalletQueryServiceImpl implements WalletQueryService{
	private final WalletRepository walletRepository;
	private final PointTransactionRepository pointTransactionRepository;
	private final PointShortageRepository pointShortageRepository;

	private static final int DEFAULT_PAGE_SIZE = 20;

	@Transactional(readOnly = true)
	public long getPoint(UUID userId) {
		Wallet wallet = walletRepository.findByUserId(userId).orElseThrow(
			() -> new WalletException(WALLET_NOT_FOUND)
		); // 추후 findAllByUserId 고려
		return wallet.checkBalance();
	}

	@Transactional(readOnly = true)
	public GetPointUsageResult getPointUsageTx(UUID orderId) {
		Optional<PointTransaction> pointTransaction =
			pointTransactionRepository.findByRefIdAndPointTxType(orderId, BUYER_PAYMENT);
		if (pointTransaction.isPresent()) {
			PointTransaction tx = pointTransaction.get();
			return GetPointUsageResult.deduct(
				orderId,
				Math.abs(tx.getChangeAmount()),
				tx.getCreatedAt()
			);
		}

		Optional<PointShortage> shortage = pointShortageRepository.findByOrderId(orderId);
		if (shortage.isPresent()) {
			PointShortage s = shortage.get();
			return GetPointUsageResult.insufficient(
				orderId,
				s.getBalance(),
				s.getShortage()
			);
		}

		return GetPointUsageResult.notFound(orderId);
	}

	@Transactional(readOnly = true)
	public GetPointTransactionPageResult getPointTransactions(UUID userId, GetPointTransactionQuery query) {
		Wallet wallet = walletRepository.findByUserId(userId).orElseThrow(
			() -> new WalletException(WALLET_NOT_FOUND)
		);
		UUID walletId = wallet.getWalletId();
		UUID cursorId = query.cursorId();
		Instant cursorTime = query.cursorTime();
		Instant to = query.to();
		Instant from = query.from();
		int size = query.size() != null ? query.size() : DEFAULT_PAGE_SIZE;

		Slice<PointTransaction> pointTransactions;

		if (cursorId == null || cursorTime == null) {
			pointTransactions = pointTransactionRepository.findFirstPointTransactions(
				walletId, from, to, Pageable.ofSize(size)
			);
		} else {
			pointTransactions = pointTransactionRepository.findNextPointTransactions(
				walletId, from, to, cursorTime, cursorId, Pageable.ofSize(size)
			);
		}
		return GetPointTransactionPageResult.from(pointTransactions);
	}
}
