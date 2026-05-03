package com.trustamarket.walletservice.wallet.presentation;

import java.util.Optional;
import java.util.UUID;

import com.trustamarket.common.config.security.UserDetailsImpl;
import com.trustamarket.common.util.SecurityUtil;
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
@RequestMapping("/internal/v1/wallets")
public class WalletInternalController {
	private final WalletCommandService walletCommandService;

	@PostMapping
	public CommonResponse<CreateWalletResponse> createWallet() { // return 타입과 파라미터 수정 필요
		UUID userId = SecurityUtil.getCurrentUserIdOrThrow();
		System.out.println(userId);
		CreateWalletResult result = walletCommandService.createWallet(userId);

		return new CommonResponse<>(HttpStatus.CREATED.value(), new CreateWalletResponse(result.walletId()));
	}

	@PatchMapping("/usages")
	public CommonResponse<UseWalletResponse> usePoint (@Valid @RequestBody UseWalletRequest request) {
		UUID userId = SecurityUtil.getCurrentUserIdOrThrow();
		UseWalletResult result = walletCommandService.usePoint(userId, new UseWalletCommand(request.orderId(), request.buyerId(), request.totalAmount()));

		return new CommonResponse(HttpStatus.OK.value(), new UseWalletResponse(result.balance(), result.shortage()));
	}

	@PostMapping("/charges")
	public CommonResponse<Void> chargePoint(@Valid @RequestBody ChargedPointRequest request){
		UUID userId = SecurityUtil.getCurrentUserIdOrThrow();
		walletCommandService.chargePoint(new ChargePointCommand(userId, request.paymentId(), request.chargedAmount()));

		return new CommonResponse(HttpStatus.OK.value(), null);
	}
}
