package com.trustamarket.walletservice.wallet.domain.entity;

import static com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode.*;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.enums.PointTxType;
import com.trustamarket.walletservice.wallet.domain.enums.RefType;
import com.trustamarket.walletservice.wallet.domain.enums.WalletStatus;
import com.trustamarket.walletservice.wallet.domain.enums.WalletType;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Table(
	name = "p_wallets",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_wallet_user_id",
			columnNames = {"user_id"}
		)
	}
)
@Entity
public class Wallet { //createdAt, updatedAt baseEntity 상속
	@Id
	@Getter
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID walletId;

	private UUID userId;

	@Embedded
	private WalletPoint balance; // 이벤트 등 종류가 늘어나면 List 고려될 수도 있음

	@Enumerated(EnumType.STRING)
	private WalletType walletType; // 이벤트 등 종류가 늘어나면 List 고려될 수도 있음

	@Enumerated(EnumType.STRING)
	private WalletStatus status;

	public static Wallet createUserWallet(UUID userId) {
		if (userId == null) {
			throw new IllegalArgumentException("userId는 필수입니다");
		}
		Wallet wallet = new Wallet();
		wallet.userId = userId;
		wallet.balance = WalletPoint.of(0);
		wallet.status = WalletStatus.ACTIVE;
		wallet.walletType = WalletType.USER;
		return wallet;
	}

	public static Wallet createSystemWallet(UUID userId) {
		if (userId == null) {
			throw new IllegalArgumentException("userId는 필수입니다");
		}
		Wallet wallet = new Wallet();
		wallet.userId = userId;
		wallet.balance = WalletPoint.of(0);
		wallet.status = WalletStatus.ACTIVE;
		wallet.walletType = WalletType.SYSTEM_ESCROW;
		return wallet;
	}

	public static Wallet createSystemFeeWallet(UUID userId) {
		if (userId == null) {
			throw new IllegalArgumentException("userId는 필수입니다");
		}
		Wallet wallet = new Wallet();
		wallet.userId = userId;
		wallet.balance = WalletPoint.of(0);
		wallet.status = WalletStatus.ACTIVE;
		wallet.walletType = WalletType.SYSTEM_FEE;
		return wallet;
	}

	public PointTransaction settleIn(long amount, UUID refId) {
		return increase(amount, refId, RefType.ORDER, PointTxType.SETTLEMENT_IN);
	}

	public PointTransaction settleOut(long amount, UUID refId) {
		return decrease(amount, refId, RefType.ORDER, PointTxType.SETTLEMENT_OUT);
	}

	public PointTransaction cancelIn(long amount, UUID refId) {
		return increase(amount, refId, RefType.ORDER, PointTxType.CANCEL_IN);
	}

	public PointTransaction cancelOut(long amount, UUID refId) {
		return decrease(amount, refId, RefType.ORDER, PointTxType.CANCEL_OUT);
	}

	public PointTransaction increaseFeeRevenue(long amount, UUID refId) {
		return increase(amount, refId, RefType.ORDER, PointTxType.FEE_REVENUE);
	}

	public PointTransaction chargeComplete(long amount, UUID refId) {
		return increase(amount, refId, RefType.PAYMENT, PointTxType.CHARGE);
	}

	public PointTransaction withdraw(long requestedAmount, long withdrawAmount, UUID refId) {
		if (withdrawAmount != requestedAmount) {
			throw new IllegalArgumentException("출금된 금액과 요청한 포인트가 다릅니다");
		}
		if(isEnough(withdrawAmount)) {
			throw new IllegalArgumentException("출금을 위한 잔액이 충분하지 않습니다.");
		}

		return decrease(withdrawAmount, refId, RefType.PAYMENT, PointTxType.WITHDRAW);
	}

	public PointTransaction increase(long amount, UUID refId, RefType refType, PointTxType txType) {
		validateActive();
		if (amount <= 0) {
			throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다");
		}

		long balanceBefore = this.balance.point();
		this.balance = this.balance.increase(amount);

		return PointTransaction.create(
			this, balanceBefore, amount, txType, refId, refType
		);
	}

	public PointTransaction decrease(long amount, UUID refId, RefType refType, PointTxType txType) {
		validateActive();

		if (amount <= 0) {
			throw new WalletException(INVALID_DEDUCTION_AMOUNT);
		}

		long balanceBefore = this.balance.point();
		this.balance = this.balance.decrease(amount);

		return PointTransaction.create(
			this, balanceBefore, -amount, txType, refId, refType
		);
	}

	private void validateActive() {
		if (status != WalletStatus.ACTIVE) {
			throw new RuntimeException("지갑이 활성 상태가 아닙니다.");
		}
	}

	public boolean isEnough(long withdrawAmount) {
		return balance.isEnough(withdrawAmount);
	}

	public boolean isNotUser() {
		return this.walletType != WalletType.USER;
	}

	public void freeze() {
		validateActive();
		this.status = WalletStatus.FROZEN;
	}

	public void close() {
		validateActive();
		if (!balance.isZero()) {
			throw new IllegalStateException("잔액이 남은 지갑은 종료할 수 없습니다");
		}
		this.status = WalletStatus.CLOSED;
	}

	public UUID getWalletOwner() {
		return this.userId;
	}

	public long checkBalance() {
		return this.balance.point();
	}
}
