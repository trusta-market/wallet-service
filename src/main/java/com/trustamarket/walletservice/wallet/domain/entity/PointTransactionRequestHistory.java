package com.trustamarket.walletservice.wallet.domain.entity;

import static com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode.*;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.enums.PointRequestStatus;
import com.trustamarket.walletservice.wallet.domain.enums.PointRequestType;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;

import jakarta.persistence.Column;
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
import lombok.Getter;

@Table(name = "p_point_transaction_request_history")
@Entity
public class PointTransactionRequestHistory {
	@Id
	@Column(name = "point_transaction_request_history_id")
	@GeneratedValue(strategy = GenerationType.UUID)
	@Getter
	private UUID pointTxRequestHistoryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "wallet_id", nullable = false)
	private Wallet wallet;

	@Enumerated(EnumType.STRING)
	private PointRequestType requestType;

	@Enumerated(EnumType.STRING)
	private PointRequestStatus status;

	@Getter
	private long requestPoint;

	public static PointTransactionRequestHistory payoutRequest(
		Wallet wallet,
		long requestPoint
	) {
		if (wallet == null) {
			throw new IllegalArgumentException("지갑은 필수");
		}
		if (requestPoint <= 0) {
			throw new IllegalArgumentException("출금 금액은 1원 이상이어야 합니다.");
		}
		if (wallet.checkBalance() < requestPoint) {
			throw new IllegalArgumentException("출금 불가능");
		}

		PointTransactionRequestHistory pointTransactionRequestHistory = new PointTransactionRequestHistory();
		pointTransactionRequestHistory.wallet = wallet;
		pointTransactionRequestHistory.requestPoint = requestPoint;
		pointTransactionRequestHistory.requestType = PointRequestType.PAYOUT;
		pointTransactionRequestHistory.status = PointRequestStatus.REQUESTED;

		return pointTransactionRequestHistory;
	}

	public void success() {
		if (this.status != PointRequestStatus.REQUESTED) {
			throw new WalletException(INVALID_STATUS_TRANSITION);
		}
		this.status = PointRequestStatus.SUCCESS;
	}

	public void fail() {
		if (this.status != PointRequestStatus.REQUESTED) {
			throw new WalletException(INVALID_STATUS_TRANSITION);
		}
		this.status = PointRequestStatus.FAILED;
	}
}
