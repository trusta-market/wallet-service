package com.trustamarket.walletservice.wallet.application.query;

import com.trustamarket.walletservice.wallet.application.dto.query.GetPointTransactionQuery;
import com.trustamarket.walletservice.wallet.application.dto.result.GetPointTransactionPageResult;

import java.util.UUID;

public interface WalletQueryService {
	long getPoint(UUID userId);
	GetPointTransactionPageResult getPointTransactions(UUID userId, GetPointTransactionQuery query);
}
