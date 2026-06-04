package com.trustamarket.walletservice.wallet.application.dto.result;

import java.time.Instant;
import java.util.UUID;

/*
"orderId":    UUID,
      "result":     "DEDUCTED" | "INSUFFICIENT" | "NOT_FOUND",
      "amount":     long (nullable),       // DEDUCTED 만 채움
      "balance":    long (nullable),       // 현재 잔액
      "shortage":   long (nullable),       // INSUFFICIENT 시 부족 금액(0아니면 null로)
      "deductedAt": Instant (nullable)     // DEDUCTED 만 채움
 */
public record GetPointUsageResult(
	UUID orderId, WalletUsageStatus walletUsageStatus,
	Long amount, Long balance, Long shortage,
	Instant deductedAt
) {
	public enum WalletUsageStatus {
		DEDUCTED, INSUFFICIENT, NOT_FOUND
	}

	public static GetPointUsageResult deduct(UUID orderId, long amount, Instant deductedAt) {
		return new GetPointUsageResult(orderId, WalletUsageStatus.DEDUCTED, amount, null, null, deductedAt);
	}

	public static GetPointUsageResult insufficient(UUID orderId, long balance, long shortage) {
		return new GetPointUsageResult(orderId, WalletUsageStatus.INSUFFICIENT, null, balance, shortage, null);
	}

	public static GetPointUsageResult notFound(UUID orderId) {
		return new GetPointUsageResult(orderId, WalletUsageStatus.NOT_FOUND, null, null, null, null);
	}
}
