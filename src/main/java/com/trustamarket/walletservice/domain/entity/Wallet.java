package com.trustamarket.walletservice.domain.entity;

import java.util.UUID;

import com.trustamarket.walletservice.domain.enums.WalletStatus;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Table(name = "p_wallets")
@Entity
public class Wallet { //createdAt, updatedAt baseEntity 상속
	@Id
	@Getter
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID walletId;

	private UUID userId;

	@Embedded
	private WalletPoint balance;

	@Enumerated(EnumType.STRING)
	private WalletStatus status;

	public static Wallet create(UUID userId) {
		if (userId == null) {
			throw new IllegalArgumentException("userId는 필수입니다");
		}
		Wallet wallet = new Wallet();
		wallet.userId = userId;
		wallet.balance = WalletPoint.of(0);
		wallet.status = WalletStatus.ACTIVE;
		return wallet;
	}

	private void validateActive() {
		if (status != WalletStatus.ACTIVE) {
			throw new RuntimeException("지갑이 활성 상태가 아닙니다.");
		}
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
}
