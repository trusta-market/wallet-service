package com.trustamarket.walletservice.wallet.application.query;

import java.util.List;
import java.util.UUID;

import com.trustamarket.walletservice.wallet.application.dto.query.GetPointTransactionQuery;
import com.trustamarket.walletservice.wallet.application.dto.result.GetPointTransactionPageResult;

public interface WalletQueryService {
	long getPoint(UUID userId);
	GetPointTransactionPageResult getPointTransactions(UUID userId, GetPointTransactionQuery query);
}
