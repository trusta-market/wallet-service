package com.trustamarket.walletservice.application.command;

import java.util.UUID;

import com.trustamarket.walletservice.application.dto.result.CreateWalletResult;

public interface WalletCommandService {
	CreateWalletResult createWallet(UUID userId);
}
