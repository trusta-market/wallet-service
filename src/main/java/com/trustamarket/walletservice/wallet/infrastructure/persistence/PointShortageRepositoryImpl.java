package com.trustamarket.walletservice.wallet.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.trustamarket.walletservice.wallet.domain.entity.PointShortage;
import com.trustamarket.walletservice.wallet.domain.repository.PointShortageRepository;
import com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa.PointShortageJpaRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class PointShortageRepositoryImpl implements PointShortageRepository {
	private final PointShortageJpaRepository pointShortageRepository;

	@Override
	public Optional<PointShortage> findByOrderId(UUID orderId) {
		return pointShortageRepository.findByOrderId(orderId);
	}
}
