package com.trustamarket.walletservice.wallet.domain.repository;

import java.util.List;

import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;

public interface PointTransactionRepository {

	List<PointTransaction> saveAll(List<PointTransaction> pointTransaction);
	PointTransaction save(PointTransaction chargeTx);
}
