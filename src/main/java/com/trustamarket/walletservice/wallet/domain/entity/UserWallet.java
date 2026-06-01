package com.trustamarket.walletservice.wallet.domain.entity;

import static com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode.*;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.enums.PointTxType;
import com.trustamarket.walletservice.wallet.domain.enums.RefType;
import com.trustamarket.walletservice.wallet.domain.enums.WalletStatus;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;

@Table(
	name = "p_user_wallets",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_user_wallet_user_id",
			columnNames = {"user_id"}
		)
	}
)
@Entity
public class UserWallet extends Wallet{
	private UUID userId;

	@Version
	@Getter
	private Long version;

	public static UserWallet createUserWallet(UUID userId) {
		if (userId == null) {
			throw new IllegalArgumentException("userId는 필수입니다");
		}
		UserWallet wallet = new UserWallet();
		wallet.userId = userId;
		wallet.balance = WalletPoint.of(0);
		wallet.status = WalletStatus.ACTIVE;
		return wallet;
	}

	public PointTransaction buyerPayment(long amount, UUID refId) {
		return decrease(amount, refId, RefType.ORDER, PointTxType.BUYER_PAYMENT);
	}

	public PointTransaction settleIn(long amount, UUID refId) {
		return increase(amount, refId, RefType.ORDER, PointTxType.SETTLEMENT_IN);
	}

	public PointTransaction cancelIn(long amount, UUID refId) {
		return increase(amount, refId, RefType.ORDER, PointTxType.CANCEL_IN);
	}

	public PointTransaction chargeComplete(long amount, UUID refId) {
		return increase(amount, refId, RefType.PAYMENT, PointTxType.CHARGE);
	}

	public PointTransaction withdraw(long requestedAmount, long withdrawAmount, UUID refId) {
		if (withdrawAmount != requestedAmount) {
			throw new IllegalArgumentException("출금된 금액과 요청한 포인트가 다릅니다");
		}
		if(!isEnough(withdrawAmount)) {
			throw new IllegalArgumentException("출금을 위한 잔액이 충분하지 않습니다.");
		}

		return decrease(withdrawAmount, refId, RefType.PAYMENT, PointTxType.WITHDRAW);
	}

	private PointTransaction increase(long amount, UUID refId, RefType refType, PointTxType txType) {
		validateActive();
		if (amount <= 0) {
			throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다");
		}

		long balanceBefore = this.balance.point();
		this.balance = this.balance.increase(amount);

		return PointTransaction.create(
			this.getWalletId(), balanceBefore, amount, txType, refId, refType
		);
	}

	private PointTransaction decrease(long amount, UUID refId, RefType refType, PointTxType txType) {
		validateActive();

		if (amount <= 0) {
			throw new WalletException(INVALID_DEDUCTION_AMOUNT);
		}

		long balanceBefore = this.balance.point();
		this.balance = this.balance.decrease(amount);

		return PointTransaction.create(
			this.getWalletId(), balanceBefore, -amount, txType, refId, refType
		);
	}

	public UUID getWalletOwner() {
		return this.userId;
	}
}
