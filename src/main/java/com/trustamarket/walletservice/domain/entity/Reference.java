package com.trustamarket.walletservice.domain.entity;

import java.util.UUID;

import com.trustamarket.walletservice.domain.enums.RefType;

import jakarta.persistence.Embeddable;

@Embeddable
record Reference(
	UUID refId,
	RefType refType
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

	static Reference withdrawal(UUID payoutId) {
		return new Reference(payoutId, RefType.PAYMENT);
	}
}
