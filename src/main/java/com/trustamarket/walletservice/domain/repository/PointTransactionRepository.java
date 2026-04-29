package com.trustamarket.walletservice.domain.repository;

import java.util.List;

import com.trustamarket.walletservice.domain.entity.PointTransaction;

public interface PointTransactionRepository {

	List<PointTransaction> saveAll(List<PointTransaction> pointTransaction);
}
