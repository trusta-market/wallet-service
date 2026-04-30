package com.trustamarket.walletservice.settlement.application.port.out;

import java.util.UUID;

public interface SettlementWalletPort {
	/**
	 * 정산에 따른 지갑 간의 포인트 이동 및 트랜잭션 저장을 위임합니다.
	 */
	void transferForSettlement(
		UUID orderId,
		UUID sellerId,
		long totalAmount,
		long sellerAmount,
		long feeAmount
	);
}
