package com.trustamarket.walletservice.wallet.presentation;

import java.util.UUID;

import com.trustamarket.walletservice.wallet.application.dto.command.ChargePointCommand;
import com.trustamarket.walletservice.wallet.presentation.dto.request.ChargedPointRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trustamarket.common.response.CommonResponse;
import com.trustamarket.walletservice.wallet.application.command.WalletCommandService;
import com.trustamarket.walletservice.wallet.application.dto.command.UseWalletCommand;
import com.trustamarket.walletservice.wallet.application.dto.result.CreateWalletResult;
import com.trustamarket.walletservice.wallet.application.dto.result.UseWalletResult;
import com.trustamarket.walletservice.wallet.presentation.dto.request.UseWalletRequest;
import com.trustamarket.walletservice.wallet.presentation.dto.response.CreateWalletResponse;
import com.trustamarket.walletservice.wallet.presentation.dto.response.UseWalletResponse;

import jakarta.validation.Valid;
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

	@PatchMapping("/usages")
	public CommonResponse<UseWalletResponse> usePoint (@Valid @RequestBody UseWalletRequest request) {
		UseWalletResult result = walletCommandService.usePoint(new UseWalletCommand(request.orderId(), request.buyerId(), request.totalAmount()));

		return new CommonResponse(HttpStatus.OK.value(), new UseWalletResponse(result.balance(), result.shortage()));
	}

	@PostMapping("/{userId}/charge")
	public CommonResponse<Void> chargePoint(@PathVariable UUID userId, @Valid @RequestBody ChargedPointRequest request){
		walletCommandService.chargePoint(new ChargePointCommand(userId, request.paymentId(), request.chargedAmount()));

		return new CommonResponse(HttpStatus.OK.value(), null);
	}
}
