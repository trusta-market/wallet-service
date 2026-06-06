package com.trustamarket.walletservice.wallet.domain.entity;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "p_point_shortage")
public class PointShortage {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Getter
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "point_transaction_request_history_id", nullable = false, unique = true)
	private PointTransactionRequestHistory requestHistory;

	@Getter
	private long shortage;

	@Getter
	private long balance; // 부족했을 당시 잔액

	public static PointShortage of(PointTransactionRequestHistory requestHistory, long shortage, long balance) {
		PointShortage pointShortage = new PointShortage();
		pointShortage.requestHistory = requestHistory;
		pointShortage.shortage = shortage;
		pointShortage.balance = balance;
		return pointShortage;
	}
}
