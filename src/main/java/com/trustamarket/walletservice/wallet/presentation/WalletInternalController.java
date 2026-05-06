package com.trustamarket.walletservice.wallet.presentation;

import com.trustamarket.common.response.CommonResponse;
import com.trustamarket.common.util.SecurityUtil;
import com.trustamarket.walletservice.wallet.application.command.WalletCommandService;
import com.trustamarket.walletservice.wallet.application.dto.command.ChargeCompleteCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.UseWalletCommand;
import com.trustamarket.walletservice.wallet.application.dto.result.CreateWalletResult;
import com.trustamarket.walletservice.wallet.application.dto.result.UseWalletResult;
import com.trustamarket.walletservice.wallet.presentation.dto.request.ChargeCompleteRequest;
import com.trustamarket.walletservice.wallet.presentation.dto.request.UseWalletRequest;
import com.trustamarket.walletservice.wallet.presentation.dto.response.CreateWalletResponse;
import com.trustamarket.walletservice.wallet.presentation.dto.response.UseWalletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/wallets")
public class WalletInternalController {
	private final WalletCommandService walletCommandService;

	@PostMapping
	public CommonResponse<CreateWalletResponse> createWallet() {
		UUID userId = SecurityUtil.getCurrentUserIdOrThrow(); //user-service로부터 받아오는 거로 수정 필요
		CreateWalletResult result = walletCommandService.createWallet(userId);

		return new CommonResponse<>(HttpStatus.CREATED.value(), new CreateWalletResponse(result.walletId()));
	}

	@PatchMapping("/usages")
	public CommonResponse<UseWalletResponse> usePoint (@Valid @RequestBody UseWalletRequest request) {
		UseWalletResult result = walletCommandService.usePoint(new UseWalletCommand(request.orderId(), request.buyerId(), request.totalAmount()));
		
		return new CommonResponse(HttpStatus.OK.value(), new UseWalletResponse(result.balance(), result.shortage()));
	}

	@PostMapping("/charges")
	public CommonResponse<Void> chargeComplete(@Valid @RequestBody ChargeCompleteRequest request){
		walletCommandService.chargeComplete(new ChargeCompleteCommand(request.userId(), request.paymentId(), request.chargeAmount()));

		return new CommonResponse(HttpStatus.OK.value(), null);
	}
}