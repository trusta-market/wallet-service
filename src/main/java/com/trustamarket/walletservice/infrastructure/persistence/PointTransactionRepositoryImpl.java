package com.trustamarket.walletservice.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.trustamarket.walletservice.domain.entity.PointTransaction;
import com.trustamarket.walletservice.domain.repository.PointTransactionRepository;
import com.trustamarket.walletservice.infrastructure.persistence.jpa.PointTransactionJpaRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class PointTransactionRepositoryImpl implements PointTransactionRepository {
	private final PointTransactionJpaRepository pointTransactionRepository;

	@Override
	public List<PointTransaction> saveAll(List<PointTransaction> pointTransaction) {
		return pointTransactionRepository.saveAll(pointTransaction);
	}
}
