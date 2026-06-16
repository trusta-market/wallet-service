package com.trustamarket.walletservice.wallet.domain.repository;

import java.util.UUID;

/**
 * 원자적 잔액 UPDATE의 결과 VO — 변경 후 잔액(balance)과 대상 walletId.
 * native UPDATE ... RETURNING wallet_id AS walletId, point AS balance 를 프로젝션으로 매핑한다.
 * (UPDATE가 user_id로 키를 잡으므로 walletId는 RETURNING으로 받아야 별도 SELECT를 안 친다.)
 */
public interface WalletBalance {
	UUID getWalletId();
	long getBalance();
}
