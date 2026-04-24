package com.trusta_market.walllet_service.domain.entity;

import java.time.Instant;
import java.util.UUID;

import com.trusta_market.walllet_service.domain.enums.PointTxStatus;
import com.trusta_market.walllet_service.domain.enums.PointTxType;
import com.trusta_market.walllet_service.domain.vo.BalanceChange;
import com.trusta_market.walllet_service.domain.vo.Reference;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Table(name = "p_point_transactions")
@Entity
public class PointTransaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private UUID pointTransactionId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "wallet_id", nullable = false)
	private Wallet wallet;

	@Embedded
	private Reference ref;

	@Embedded
	private BalanceChange balanceChange;

	@Enumerated(EnumType.STRING)
	@Column(name = "tx_type", nullable = false)
	private PointTxType pointTxType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PointTxStatus status;

	private Instant createdAt;

}
