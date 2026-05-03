package com.trustamarket.walletservice.wallet.application.command;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.application.dto.command.ChargePointCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.UseWalletCommand;
import com.trustamarket.walletservice.wallet.application.dto.result.CreateWalletResult;
import com.trustamarket.walletservice.wallet.application.dto.result.UseWalletResult;

public interface WalletCommandService {
	CreateWalletResult createWallet(UUID userId);
	UseWalletResult usePoint(UseWalletCommand command);
	void chargePoint(ChargePointCommand command);

	void transferForSettlement(UUID orderId, UUID sellerId, long totalAmount, long sellerAmount, long feeAmount);
}