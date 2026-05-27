package com.trustamarket.walletservice.wallet.domain.entity;

import static com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode.*;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.enums.PointTxType;
import com.trustamarket.walletservice.wallet.domain.enums.RefType;
import com.trustamarket.walletservice.wallet.domain.enums.SystemWalletType;
import com.trustamarket.walletservice.wallet.domain.enums.WalletStatus;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;

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

	public PointTransaction settleOut(long amount, UUID refId) {
		if(!isEnough(amount)) {
			throw new IllegalArgumentException("정산을 위한 잔액이 충분하지 않습니다.");
		}
		return decrease(amount, refId, RefType.ORDER, PointTxType.SETTLEMENT_OUT);
	}

	public PointTransaction cancelOut(long amount, UUID refId) {
		if(!isEnough(amount)) {
			throw new IllegalArgumentException("취소를 위한 잔액이 충분하지 않습니다.");
		}
		return decrease(amount, refId, RefType.ORDER, PointTxType.CANCEL_OUT);
	}

	public PointTransaction increaseFeeRevenue(long amount, UUID refId) {
		return increase(amount, refId, RefType.ORDER, PointTxType.FEE_REVENUE);
	}

	public PointTransaction increasePointSource(long amount, UUID refId) {
		return increase(amount, refId, RefType.PAYMENT, PointTxType.POINT_SOURCE_IN);
	}

	public PointTransaction decreasePointSource(long amount, UUID refId) {
		//최종 잔액 음수 가능
		validateActive();
		if (amount <= 0) {
			throw new WalletException(INVALID_DEDUCTION_AMOUNT);
		}
		long balanceBefore = this.balance.point();
		this.balance = this.balance.systemPointResourceDecrease(amount); // 음수 가능
		return PointTransaction.create(
			this.getWalletId(), balanceBefore, -amount, PointTxType.POINT_SOURCE_OUT, refId, RefType.PAYMENT
		);
	}
}
