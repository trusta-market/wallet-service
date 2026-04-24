package com.trusta_market.walllet_service.domain.vo;

import jakarta.persistence.Embeddable;

@Embeddable
public record BalanceChange (
	Long balance,
	Long afterBalance
) {
}
