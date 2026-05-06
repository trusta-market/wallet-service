package com.trustamarket.walletservice.wallet.infrastructure.persistence;

import java.awt.*;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;
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
}
