package com.trustamarket.walletservice.wallet.presentation;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trustamarket.common.response.CommonResponse;
import com.trustamarket.common.util.SecurityUtil;
import com.trustamarket.walletservice.wallet.application.command.SystemWalletCommandService;
import com.trustamarket.walletservice.wallet.application.command.WalletCommandService;
import com.trustamarket.walletservice.wallet.application.dto.command.ChargePointCommand;
import com.trustamarket.walletservice.wallet.application.dto.command.WithdrawPointCommand;
import com.trustamarket.walletservice.wallet.application.dto.creator.CreateSystemWalletDto;
import com.trustamarket.walletservice.wallet.application.dto.result.ChargePointResult;
import com.trustamarket.walletservice.wallet.application.dto.result.WithdrawPointResult;
import com.trustamarket.walletservice.wallet.domain.entity.Wallet;
import com.trustamarket.walletservice.wallet.presentation.dto.request.ChargePointRequest;
import com.trustamarket.walletservice.wallet.presentation.dto.request.CreateSystemWalletRequest;
import com.trustamarket.walletservice.wallet.presentation.dto.request.WithdrawPointRequest;
import com.trustamarket.walletservice.wallet.presentation.dto.response.ChargePointResponse;
import com.trustamarket.walletservice.wallet.presentation.dto.response.CreateWalletResponse;
import com.trustamarket.walletservice.wallet.presentation.dto.response.WithdrawPointResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallets")
public class WalletController {
	private final WalletCommandService walletCommandService;
	private final SystemWalletCommandService systemWalletService;

	@PostMapping("/charges")
	public CommonResponse<ChargePointResponse> chargePoint(@Valid @RequestBody ChargePointRequest request){
		UUID userId = SecurityUtil.getCurrentUserIdOrThrow();
		UUID paymentId = UUID.randomUUID();

		ChargePointCommand command = new ChargePointCommand(userId, paymentId, request.chargeAmount());
		ChargePointResult result = walletCommandService.chargePoint(command);

		ChargePointResponse response = ChargePointResponse.from(result);

		return new CommonResponse(HttpStatus.OK.value(), response);
	}

	@PostMapping("/withdrawals")
	public CommonResponse<WithdrawPointResponse> withdrawRequest(@Valid @RequestBody WithdrawPointRequest request) {
		UUID userId = SecurityUtil.getCurrentUserIdOrThrow();
		WithdrawPointResult result = walletCommandService.withdrawPoint(
			WithdrawPointCommand.of(userId, request.pointTxRequestHistoryId(), request.withdrawAmount())
		);
		return new CommonResponse<>(HttpStatus.ACCEPTED.value(), WithdrawPointResponse.from(result)); //PRG + body 비우기
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/system")
	public CommonResponse<CreateWalletResponse> createSystemWallet(@Valid @RequestBody CreateSystemWalletRequest request) {
		Wallet result = systemWalletService.createSystemWallet(
			CreateSystemWalletDto.of(request.operatorId(), request.walletType())
		);

		return new CommonResponse<>(HttpStatus.CREATED.value(), CreateWalletResponse.from(result));
	}
}
