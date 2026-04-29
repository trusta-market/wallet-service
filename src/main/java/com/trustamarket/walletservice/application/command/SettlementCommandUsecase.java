package com.trustamarket.walletservice.application.command;

import com.trustamarket.walletservice.application.dto.message.SettlePointSettlementMessage;

public interface SettlementCommandUsecase {
	void process(SettlePointSettlementMessage message);
}
