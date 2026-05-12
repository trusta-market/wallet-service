package com.trustamarket.walletservice.wallet.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.trustamarket.common.response.CommonResponse;
import com.trustamarket.walletservice.wallet.application.command.WalletCommandService;
import com.trustamarket.walletservice.wallet.application.dto.command.ChargeCompleteCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.UseWalletCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.WithdrawCompleteCommand;
import com.trustamarket.walletservice.wallet.application.dto.result.CreateWalletResult;
import com.trustamarket.walletservice.wallet.application.dto.result.UseWalletResult;
import com.trustamarket.walletservice.wallet.domain.enums.PointRequestStatus;
import com.trustamarket.walletservice.wallet.presentation.dto.request.ChargeCompleteRequest;
import com.trustamarket.walletservice.wallet.presentation.dto.request.CreateWalletRequest;
import com.trustamarket.walletservice.wallet.presentation.dto.request.UseWalletRequest;
import com.trustamarket.walletservice.wallet.presentation.dto.request.WithdrawCompleteRequest;
import com.trustamarket.walletservice.wallet.presentation.dto.response.CreateWalletResponse;
import com.trustamarket.walletservice.wallet.presentation.dto.response.UseWalletResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/wallets")
public class WalletInternalController {
	private final WalletCommandService walletCommandService;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public CreateWalletResponse createWallet(@RequestBody CreateWalletRequest request) {
		CreateWalletResult createResult = walletCommandService.createWallet(request.userId());
		return new CreateWalletResponse(createResult.result());
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

	@PostMapping("/withdrawals")
	public CommonResponse<Void> withdrawComplete(@Valid @RequestBody WithdrawCompleteRequest request) {
		walletCommandService.withdrawComplete(WithdrawCompleteCommand.of(
			request.userId(),
			request.payoutId(),
			request.pointTxRequestHistoryId(),
			PointRequestStatus.from(request.payoutStatus()),
			request.payoutAmount()
		));
		return new CommonResponse<>(HttpStatus.NO_CONTENT.value(), null);
	}
}