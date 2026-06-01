package com.trustamarket.walletservice.wallet.domain.entity;

import java.time.Instant;
import java.util.UUID;

import com.trustamarket.common.domain.BaseCreatedEntity;
import com.trustamarket.walletservice.wallet.domain.enums.PointTxType;
import com.trustamarket.walletservice.wallet.domain.enums.RefType;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "p_point_transactions",
	uniqueConstraints = {
		@UniqueConstraint(
			name = PointTransaction.CANCEL_UNIQUE_CONSTRAINT, // 부분 환불, 결제가 정책 상 존재X (YAGNI)
			columnNames = {"ref_id", "tx_type"}
		)
	}
)
@Entity
public class PointTransaction extends BaseCreatedEntity {
	public static final String CANCEL_UNIQUE_CONSTRAINT = "uk_point_tx_ref_type";

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Getter
	private UUID pointTransactionId;

	// @ManyToOne(fetch = FetchType.LAZY)
	// @JoinColumn(name = "wallet_id", nullable = false)
	// private Wallet wallet; -> wallet 종류 나누며 테이블 분리로 fk 불가능

	@Column(name = "wallet_id", nullable = false)
	private UUID walletId;

	@Embedded
	private Reference ref;

	@Embedded
	private BalanceChange balanceChange;

	@Getter
	@Enumerated(EnumType.STRING)
	@Column(name = "tx_type", nullable = false)
	private PointTxType pointTxType;

	private Instant processedAt;

	public static PointTransaction create(
		UUID walletId,
		long balanceBefore,
		long changedBalance,
		PointTxType pointTxType,
		UUID refId,
		RefType refType
	) {
		if (walletId == null) {
			throw new IllegalArgumentException("지갑은 필수");
		}
		if (pointTxType == null) {
			throw new IllegalArgumentException("트랜잭션 타입은 필수");
		}
		validateTypeAndAmount(pointTxType, changedBalance);

		PointTransaction tx = new PointTransaction();
		tx.walletId = walletId;
		tx.balanceChange = BalanceChange.of(balanceBefore, changedBalance);
		tx.pointTxType = pointTxType;
		tx.ref = Reference.of(refId, refType);
		return tx;
	}

	// 주 트랜잭션용 — balance 건드리지 않고 PT만 생성
	public static PointTransaction createSystemWalletTx(
		UUID walletId,
		long changedBalance,
		PointTxType pointTxType,
		UUID refId,
		RefType refType
	) {
		if (walletId == null) {
			throw new IllegalArgumentException("지갑은 필수");
		}
		if (pointTxType == null) {
			throw new IllegalArgumentException("트랜잭션 타입은 필수");
		}
		validateTypeAndAmount(pointTxType, changedBalance);

		PointTransaction tx = new PointTransaction();
		tx.walletId = walletId;
		tx.balanceChange = BalanceChange.of(changedBalance);
		tx.pointTxType = pointTxType;
		tx.ref = Reference.of(refId, refType);
		return tx;
	}

	private static void validateTypeAndAmount(PointTxType type, long amount) {
		if (type.isIncrease() && amount <= 0) {
			throw new IllegalArgumentException("타입은 양수여야 함.");
		}
		if (type.isDecrease() && amount >= 0) {
			throw new IllegalArgumentException("타입은 음수여야 함.");
		}
	}

	public long getChangeAmount() {
		return this.balanceChange.amount();
	}
}
