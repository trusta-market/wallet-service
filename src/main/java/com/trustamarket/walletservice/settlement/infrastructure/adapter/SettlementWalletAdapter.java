package com.trustamarket.walletservice.settlement.infrastructure.adapter;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.trustamarket.walletservice.settlement.application.port.out.SettlementWalletPort;
import com.trustamarket.walletservice.wallet.application.command.WalletCommandService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SettlementWalletAdapter implements SettlementWalletPort {
	private final WalletCommandService walletCommandService;
	@Override
	public void transferForSettlement(UUID orderId, UUID sellerId, long totalAmount, long sellerAmount, long feeAmount) {
		walletCommandService.transferForSettlement(orderId, sellerId, totalAmount, sellerAmount, feeAmount);

	}
}
