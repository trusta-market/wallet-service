package com.trustamarket.walletservice.wallet.presentation;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trustamarket.common.response.CommonResponse;
import com.trustamarket.walletservice.wallet.application.command.WalletCommandService;
import com.trustamarket.walletservice.wallet.application.dto.command.ChargeCompleteCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.UseWalletCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.WithdrawCompleteCommand;
import com.trustamarket.walletservice.wallet.application.dto.result.CreateWalletResult;
import com.trustamarket.walletservice.wallet.application.dto.result.GetPointUsageResult;
import com.trustamarket.walletservice.wallet.application.dto.result.UseWalletResult;
import com.trustamarket.walletservice.wallet.application.query.WalletQueryService;
import com.trustamarket.walletservice.wallet.domain.enums.PointRequestStatus;
import com.trustamarket.walletservice.wallet.presentation.dto.request.ChargeCompleteRequest;
import com.trustamarket.walletservice.wallet.presentation.dto.request.CreateWalletRequest;
import com.trustamarket.walletservice.wallet.presentation.dto.request.UseWalletRequest;
import com.trustamarket.walletservice.wallet.presentation.dto.request.WithdrawCompleteRequest;
import com.trustamarket.walletservice.wallet.presentation.dto.response.CreateWalletResponse;
import com.trustamarket.walletservice.wallet.presentation.dto.response.GetPointUsageResponse;
import com.trustamarket.walletservice.wallet.presentation.dto.response.UseWalletResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/wallets")
public class WalletInternalController {
	private final WalletCommandService walletCommandService;
	private final WalletQueryService walletQueryService;

	@PostMapping
	public ResponseEntity<CommonResponse<CreateWalletResponse>> createWallet(@RequestBody CreateWalletRequest request) {
		CreateWalletResult createResult = walletCommandService.createWallet(request.userId());
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(CommonResponse.of(HttpStatus.CREATED.value(), new CreateWalletResponse(createResult.result())));
	}

	@GetMapping("/usages") // requestParam
	public ResponseEntity<CommonResponse<GetPointUsageResponse>> getPointUsageTransaction(@RequestParam(required = true) UUID orderId) {
		GetPointUsageResult result = walletQueryService.getPointUsageTx(orderId);
		return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK.value(), GetPointUsageResponse.from(result)));
	}


	@PatchMapping("/usages")
	public ResponseEntity<CommonResponse<UseWalletResponse>> usePoint (@Valid @RequestBody UseWalletRequest request) {
		UseWalletResult result = walletCommandService.usePoint(new UseWalletCommand(request.idempotencyKey(), request.orderId(), request.buyerId(), request.totalAmount()));
		
		return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK.value(), new UseWalletResponse(result.balance(), result.shortage())));
	}

	@PostMapping("/charges")
	public ResponseEntity<Void> chargeComplete(@Valid @RequestBody ChargeCompleteRequest request){
		walletCommandService.chargeComplete(new ChargeCompleteCommand(
				request.userId(),
				request.paymentId(),
				request.pointTxRequestHistoryId(),
				request.paymentStatus(),
				request.chargeAmount()
		));

		return ResponseEntity.noContent().build();
	}

	@PostMapping("/withdrawals")
	public ResponseEntity<Void> withdrawComplete(@Valid @RequestBody WithdrawCompleteRequest request) {
		walletCommandService.withdrawComplete(WithdrawCompleteCommand.of(
			request.userId(),
			request.payoutId(),
			request.pointTxRequestHistoryId(),
			PointRequestStatus.from(request.payoutStatus()),
			request.payoutAmount()
		));
		return ResponseEntity.noContent().build();
	}
}