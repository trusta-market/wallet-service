package com.trustamarket.walletservice.wallet.application.command;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.application.dto.result.CreateWalletResult;

public interface WalletCommandService {
	CreateWalletResult createWallet(UUID userId);
}
