package com.trusta_market.walllet_service.domain.entity;

import java.util.UUID;

import com.trusta_market.walllet_service.domain.enums.WalletStatus;
import com.trusta_market.walllet_service.domain.vo.Point;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "p_wallets")
@Entity
public class Wallet { //createdAt, updatedAt baseEntity 상속
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID walletId;

	private UUID userId;

	@Embedded
	private Point balance;

	@Enumerated(EnumType.STRING)
	private WalletStatus status;
}
