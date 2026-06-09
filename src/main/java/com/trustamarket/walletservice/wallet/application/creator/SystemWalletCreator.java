package com.trustamarket.walletservice.wallet.application.creator;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.domain.entity.SystemWallet;
import com.trustamarket.walletservice.wallet.domain.enums.SystemWalletType;

public interface SystemWalletCreator {
	SystemWalletType getType();
	SystemWallet create(UUID operatorId);
}
