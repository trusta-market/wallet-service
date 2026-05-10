package com.trustamarket.walletservice.wallet.application.command;

import com.trustamarket.walletservice.wallet.application.dto.message.CancelMessage;

public interface WalletMessageUsecase {
	void cancelProcess(CancelMessage message);
}
