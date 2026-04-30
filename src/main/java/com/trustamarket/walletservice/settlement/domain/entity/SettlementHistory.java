package com.trustamarket.walletservice.settlement.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.trustamarket.walletservice.settlement.domain.enums.PointSettlementStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(
	name = "p_settlement_history"
	// uniqueConstraints = { // 이름 바뀔 가능성 때문에 우선 @Column에 unique로 표시
	// 	@UniqueConstraint(
	// 		name = "uk_settlement_event_id",
	// 		columnNames = {"event_id"}
	// 	)
	// }
)
public class SettlementHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID settlementHistoryId;

	// inbox와 exactly-once 고려, orderId+status고려
	@Column(nullable = false, unique = true)
	private UUID eventId;

	@Column(nullable = false)
	private UUID orderId;

	@Column(nullable = false)
	private UUID sellerId;

	@Embedded
	private SettlementAmount settlementAmount;

	@Column(precision = 5, scale = 4)
	private BigDecimal appliedFeeRate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PointSettlementStatus status;

	@Column(name = "processed_at", nullable = false)
	private Instant processedAt;

	public static SettlementHistory complete(
		UUID eventId,
		UUID orderId,
		UUID sellerId,
		long totalAmount,
		long sellerAmount,
		long feeRevenueAmount,
		BigDecimal appliedFeeRate
	) {
		if (eventId == null || orderId == null || sellerId == null) {
			throw new IllegalArgumentException("정산 식별자는 필수입니다.");
			}


		if (appliedFeeRate != null
			&& (appliedFeeRate.signum() < 0 || appliedFeeRate.compareTo(BigDecimal.ONE) > 0)) {
			throw new IllegalArgumentException("수수료율은 0 이상 1 이하여야 합니다.");
		}
		SettlementHistory history = new SettlementHistory();
		history.eventId = eventId;
		history.orderId = orderId;
		history.sellerId = sellerId;
		history.settlementAmount = SettlementAmount.of(totalAmount, sellerAmount, feeRevenueAmount);
		history.appliedFeeRate = appliedFeeRate;
		history.status = PointSettlementStatus.COMPLETED;
		history.processedAt = Instant.now();
		return history;
	}


}
