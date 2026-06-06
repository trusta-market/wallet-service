package com.trustamarket.walletservice.wallet.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.entity.PointShortage;

public interface PointShortageRepository {
	Optional<PointShortage> findByOrderId(UUID orderId);
}
