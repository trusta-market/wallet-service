package com.trustamarket.walletservice.wallet.domain.entity;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.enums.RefType;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
record Reference(
	UUID refId,
	@Enumerated(EnumType.STRING) RefType refType
) {
	Reference {
		if (refId == null) {
			throw new IllegalArgumentException("참조 ID는 필수입니다");
		}
		if (refType == null) {
			throw new IllegalArgumentException("참조 타입은 필수입니다");
		}
	}

	static Reference of(UUID refId, RefType refType) {
		return new Reference(refId, refType);
	}

	static Reference order(UUID orderId) {
		return new Reference(orderId, RefType.ORDER);
	}

	/**
	 * 출금(Withdrawal) 행위에 대한 Reference를 생성
	 * 출금은 PAYMENT의 데이터와 연관되어 참조하므로 RefType.PAYMENT를 사용
	 * 결제와 point변화 관계를 알기 위해 사용
	 * 
	 * `@param` payoutId 참조하는 Payment 객체의 ID
	 */
	static Reference withdrawal(UUID payoutId) {
		return new Reference(payoutId, RefType.PAYMENT);
	}

	static Reference charge(UUID paymentId) { return new Reference(paymentId, RefType.PAYMENT); }
}
