package com.trustamarket.walletservice.settlement.application.command;

import com.trustamarket.walletservice.settlement.application.dto.message.SettlePointSettlementMessage;

public interface SettlementCommandUsecase {
	void process(SettlePointSettlementMessage message);
}
