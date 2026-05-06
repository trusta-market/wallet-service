package com.trustamarket.walletservice.wallet.application.creator;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.entity.Wallet;
import com.trustamarket.walletservice.wallet.domain.enums.WalletType;

public interface SystemWalletCreator {
	WalletType getType();
	Wallet create(UUID operatorId);
}
