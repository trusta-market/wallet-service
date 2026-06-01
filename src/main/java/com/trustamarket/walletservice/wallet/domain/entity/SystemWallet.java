package com.trustamarket.walletservice.wallet.domain.entity;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.enums.PointTxType;
import com.trustamarket.walletservice.wallet.domain.enums.RefType;
import com.trustamarket.walletservice.wallet.domain.enums.SystemWalletType;
import com.trustamarket.walletservice.wallet.domain.enums.WalletStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Table(name = "p_system_wallets")
@Entity
public class SystemWallet extends Wallet {
	@Enumerated(EnumType.STRING)
	private SystemWalletType systemWalletType;

	private UUID creatorId;

	public static SystemWallet createSystemWallet(UUID creatorId) {
		if (creatorId == null) {
			throw new IllegalArgumentException("userId는 필수입니다");
		}
		SystemWallet wallet = new SystemWallet();
		wallet.creatorId = creatorId;
		wallet.balance = WalletPoint.of(0);
		wallet.status = WalletStatus.ACTIVE;
		wallet.systemWalletType = SystemWalletType.SYSTEM_ESCROW;
		return wallet;
	}

	public static SystemWallet createSystemFeeWallet(UUID creatorId) {
		if (creatorId == null) {
			throw new IllegalArgumentException("userId는 필수입니다");
		}
		SystemWallet wallet = new SystemWallet();
		wallet.creatorId = creatorId;
		wallet.balance = WalletPoint.of(0);
		wallet.status = WalletStatus.ACTIVE;
		wallet.systemWalletType = SystemWalletType.SYSTEM_FEE;
		return wallet;
	}

	public static SystemWallet createSystemPointSourceWallet(UUID creatorId) {
		SystemWallet wallet = new SystemWallet();
		wallet.creatorId = creatorId;
		wallet.balance = WalletPoint.of(0);
		wallet.status = WalletStatus.ACTIVE;
		wallet.systemWalletType = SystemWalletType.SYSTEM_POINT_SOURCE;
		return wallet;
	}

	public PointTransaction recordEscrowDepositPointTx(long amount, UUID refId) {
		validateActive();
		return PointTransaction.createSystemWalletTx(
			this.getWalletId(), amount, PointTxType.ESCROW_DEPOSIT, refId, RefType.ORDER
		);
	}

	public PointTransaction recordSettleOutTx(long amount, UUID refId) {
		validateActive();
		return PointTransaction.createSystemWalletTx(this.getWalletId(), -amount, PointTxType.SETTLEMENT_OUT, refId, RefType.ORDER);
	}

	public PointTransaction recordCancelOutTx(long amount, UUID refId) {
		validateActive();
		return PointTransaction.createSystemWalletTx(this.getWalletId(), -amount, PointTxType.CANCEL_OUT, refId, RefType.ORDER);
	}

	public PointTransaction recordIncreaseFeeRevenueTx(long amount, UUID refId) {
		validateActive();
		return PointTransaction.createSystemWalletTx(this.getWalletId(), amount, PointTxType.FEE_REVENUE, refId, RefType.ORDER);
	}

	public PointTransaction recordIncreasePointSourceTx(long amount, UUID refId) {
		validateActive();
		return PointTransaction.createSystemWalletTx(this.getWalletId(), amount, PointTxType.POINT_SOURCE_IN, refId, RefType.PAYMENT);
	}

	public PointTransaction recordDecreasePointSourceTx(long amount, UUID refId) {
		//최종 잔액 음수 가능
		validateActive();
		return PointTransaction.createSystemWalletTx(this.getWalletId(), -amount, PointTxType.POINT_SOURCE_OUT, refId, RefType.PAYMENT);
	}

	// outbox relay용 — PointTx 생성 없이 balance만 업데이트
	public void applyBalanceDelta(long deltaAmount) {
		validateActive();
		this.balance = WalletPoint.of(this.balance.point() + deltaAmount);
	}
}
