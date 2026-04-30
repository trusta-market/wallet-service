package com.trustamarket.walletservice.wallet.application.dto.result;

public record UseWalletResult(
	Long balance,
	Long shortage  // 부족 시에만 값
) {
	public static UseWalletResult success(long balance) {
		return new UseWalletResult(balance, null);
	}

	public static UseWalletResult insufficient(long balance, long shortage) {
		return new UseWalletResult(balance, shortage);
	}
}
