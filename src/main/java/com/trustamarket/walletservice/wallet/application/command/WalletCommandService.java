package com.trustamarket.walletservice.wallet.application.command;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.application.dto.command.ChargeCompleteCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.ChargePointCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.UseWalletCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.WithdrawCompleteCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.WithdrawPointCommand;
import com.trustamarket.walletservice.wallet.application.dto.result.ChargePointResult;
import com.trustamarket.walletservice.wallet.application.dto.result.CreateWalletResult;
import com.trustamarket.walletservice.wallet.application.dto.result.UseWalletResult;
import com.trustamarket.walletservice.wallet.application.dto.result.WithdrawPointResult;

public interface WalletCommandService {
	CreateWalletResult createWallet(UUID userId);
	UseWalletResult usePoint(UseWalletCommand command);

	ChargePointResult chargePoint(ChargePointCommand command);
	void chargeComplete(ChargeCompleteCommand command);

	void transferForSettlement(UUID orderId, UUID sellerId, long totalAmount, long sellerAmount, long feeAmount);

	WithdrawPointResult withdrawPoint(WithdrawPointCommand command);
	void withdrawComplete(WithdrawCompleteCommand command);
}