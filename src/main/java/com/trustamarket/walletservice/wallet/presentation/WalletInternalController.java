package com.trustamarket.walletservice.wallet.presentation;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trustamarket.common.response.CommonResponse;
import com.trustamarket.walletservice.wallet.application.command.WalletCommandService;
import com.trustamarket.walletservice.wallet.application.dto.result.CreateWalletResult;
import com.trustamarket.walletservice.wallet.presentation.dto.response.CreateWalletResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/wallets")
public class WalletInternalController {
	private final WalletCommandService walletCommandService;

	@PostMapping
	public CommonResponse<CreateWalletResponse> createWallet(UUID userId) { // return 타입과 파라미터 수정 필요

		CreateWalletResult result = walletCommandService.createWallet(userId);

		return new CommonResponse<>(HttpStatus.CREATED.value(), new CreateWalletResponse(result.walletId()));
	}
}
