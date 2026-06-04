package com.trustamarket.walletservice.wallet.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;
import com.trustamarket.walletservice.wallet.domain.enums.PointTxType;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRepository;
import com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa.PointTransactionJpaRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class PointTransactionRepositoryImpl implements PointTransactionRepository {
	private final PointTransactionJpaRepository pointTransactionRepository;

	@Override
	public List<PointTransaction> saveAll(List<PointTransaction> pointTransaction) {
		return pointTransactionRepository.saveAll(pointTransaction);
	}

	@Override
	public PointTransaction save(PointTransaction chargeTx) {
		return pointTransactionRepository.save(chargeTx);
	}

	@Override
	public Slice<PointTransaction> findFirstPointTransactions(UUID walletId, Instant from, Instant to, Pageable pageable) {
		return  pointTransactionRepository.findFirstPointTransactions(walletId, from, to, pageable);
	}

	@Override
	public Slice<PointTransaction> findNextPointTransactions(
		UUID walletId, Instant from, Instant to,
		Instant cursorTime, UUID cursorId, Pageable pageable) {
		return  pointTransactionRepository.findNextPointTransactions(walletId, from, to, cursorTime, cursorId, pageable);
	}

	@Override
	public boolean existsByRefIdAndPointTxType(UUID orderId, PointTxType pointTxType) {
		return pointTransactionRepository.existsByRefIdAndPointTxType(orderId, pointTxType);
	}

	@Override
	public Optional<PointTransaction> findByOrderIdAndWalletIdAndPointTxType(UUID orderId, UUID walletId, PointTxType pointTxType) {
		return pointTransactionRepository.findByOrderIdAndWalletIdAndPointTxType(orderId, walletId, pointTxType);
	}

	@Override
	public Optional<PointTransaction> findByRefIdAndPointTxType(UUID refId, PointTxType pointTxType) {
		return pointTransactionRepository.findByRefIdAndPointTxType(refId, pointTxType);
	}
}
