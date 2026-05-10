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
import com.trustamarket.walletservice.wallet.application.dto.query.GetPointTransactionQuery;
import com.trustamarket.walletservice.wallet.application.dto.result.GetPointTransactionPageResult;
import com.trustamarket.walletservice.wallet.application.query.WalletQueryService;
import com.trustamarket.walletservice.wallet.presentation.dto.request.GetPointTransactionsRequest;
import com.trustamarket.walletservice.wallet.presentation.dto.response.ChargePointResponse;
import com.trustamarket.walletservice.wallet.presentation.dto.response.CreateWalletResponse;
import com.trustamarket.walletservice.wallet.presentation.dto.response.WithdrawPointResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallets")
public class WalletController {
	private final WalletCommandService walletCommandService;
	private final SystemWalletCommandService systemWalletService;
	private final WalletQueryService walletQueryService;

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
		return new CommonResponse<>(HttpStatus.ACCEPTED.value(), WithdrawPointResponse.from(result));
	}

	@GetMapping("/transactions")
	public CommonResponse<GetPointTransactionPageResult> getPointTransactions(
		@Valid @ModelAttribute GetPointTransactionsRequest queryRequest
	) {
		UUID userId = SecurityUtil.getCurrentUserIdOrThrow();
		GetPointTransactionPageResult result = walletQueryService.getPointTransactions(
			userId,
			GetPointTransactionQuery.of(
				queryRequest.getValidFrom(), queryRequest.getValidTo(),
				queryRequest.cursorTime(), queryRequest.cursorId(),
				queryRequest.size())
		);
		return new CommonResponse<>(HttpStatus.OK.value(), result);
	}

	@GetMapping("/balances")
	public CommonResponse<Long> getPointBalance() {
		UUID userId = SecurityUtil.getCurrentUserIdOrThrow();
		long balance = walletQueryService.getPoint(userId);
		return new CommonResponse<>(HttpStatus.OK.value(), balance);
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
