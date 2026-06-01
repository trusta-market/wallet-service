package com.trustamarket.walletservice.wallet.domain.entity;

import java.time.Instant;
import java.util.UUID;

import com.trustamarket.common.domain.BaseCreatedEntity;
import com.trustamarket.walletservice.wallet.domain.enums.OutboxStatus;
import com.trustamarket.walletservice.wallet.domain.enums.PointTxType;
import com.trustamarket.walletservice.wallet.domain.enums.RefType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_system_wallet_outbox")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SystemWalletOutbox extends BaseCreatedEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID outboxId;
	private UUID walletId;
	private long deltaAmount;
	@Enumerated(EnumType.STRING)
	private PointTxType txType;
	private UUID refId;
	@Enumerated(EnumType.STRING)
	private RefType refType;
	@Enumerated(EnumType.STRING)
	private OutboxStatus outboxStatus;
	private int retryCount;
	private Instant processedAt;

	public static SystemWalletOutbox create(
		UUID walletId, long deltaAmount, PointTxType pointTxType,
		UUID refId, RefType refType
	) {
		SystemWalletOutbox systemWalletOutbox = new SystemWalletOutbox();
		systemWalletOutbox.walletId = walletId;
		systemWalletOutbox.deltaAmount = deltaAmount;
		systemWalletOutbox.txType = pointTxType;
		systemWalletOutbox.refId = refId;
		systemWalletOutbox.refType = refType;
		systemWalletOutbox.outboxStatus = OutboxStatus.PENDING;
		systemWalletOutbox.retryCount = 0;
		return systemWalletOutbox;
	}

	public void markApplied() {
		this.outboxStatus = OutboxStatus.APPLIED;
		this.processedAt = Instant.now();
	}

	public void markFailed() {
		this.outboxStatus = OutboxStatus.FAILED;
	}

	public int increaseRetrycount() {
		return ++this.retryCount;
	}
}
