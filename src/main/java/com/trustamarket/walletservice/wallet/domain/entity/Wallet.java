package com.trustamarket.walletservice.wallet.domain.entity;

import java.util.UUID;

import com.trustamarket.common.domain.BaseTimeEntity;
import com.trustamarket.walletservice.wallet.domain.enums.WalletStatus;

import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass
public abstract class Wallet extends BaseTimeEntity {
	@Id
	@Getter
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID walletId;

	@Embedded
	protected WalletPoint balance; // 이벤트 등 종류가 늘어나면 List 고려될 수도 있음, 서브 클래스 접근

	@Enumerated(EnumType.STRING)
	protected WalletStatus status;



	protected void validateActive() {
		if (status != WalletStatus.ACTIVE) {
			throw new RuntimeException("지갑이 활성 상태가 아닙니다.");
		}
	}

	public boolean isEnough(long withdrawAmount) {
		return balance.isEnough(withdrawAmount);
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

	public long checkBalance() {
		return this.balance.point();
	}
}
