package com.trustamarket.walletservice.wallet.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trustamarket.walletservice.wallet.domain.entity.PointShortage;

public interface PointShortageJpaRepository extends JpaRepository<PointShortage, UUID> {

	@Query("SELECT s FROM PointShortage s JOIN FETCH s.requestHistory rh WHERE rh.refId = :orderId")
	Optional<PointShortage> findByOrderId(@Param("orderId") UUID orderId);
}
