package com.trustamarket.walletservice.wallet.application.query;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.application.dto.query.GetPointTransactionQuery;
import com.trustamarket.walletservice.wallet.application.dto.result.GetPointTransactionPageResult;
import com.trustamarket.walletservice.wallet.application.dto.result.GetPointUsageResult;

public interface WalletQueryService {
	long getPoint(UUID userId);
	GetPointTransactionPageResult getPointTransactions(UUID userId, GetPointTransactionQuery query);
	GetPointUsageResult getPointUsageTx(UUID orderId);
}
