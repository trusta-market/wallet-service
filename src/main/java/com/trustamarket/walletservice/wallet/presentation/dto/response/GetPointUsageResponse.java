package com.trustamarket.walletservice.wallet.presentation.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.trustamarket.walletservice.wallet.application.dto.result.GetPointUsageResult;

public record GetPointUsageResponse(UUID orderId, String result,
								 Long amount, Long balance, Long shortage,
								 Instant deductedAt)
{

	public static GetPointUsageResponse from(GetPointUsageResult pointUsageResult) {
		return new GetPointUsageResponse(
			pointUsageResult.orderId(),
			pointUsageResult.walletUsageStatus().name(),
			pointUsageResult.amount(),
			pointUsageResult.balance(),
			pointUsageResult.shortage(),
			pointUsageResult.deductedAt()
		);
	}
}
