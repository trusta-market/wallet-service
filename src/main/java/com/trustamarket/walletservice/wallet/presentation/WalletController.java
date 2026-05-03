package com.trustamarket.walletservice.wallet.presentation;

import com.trustamarket.common.response.CommonResponse;
import com.trustamarket.common.util.SecurityUtil;
import com.trustamarket.walletservice.wallet.application.command.WalletCommandService;
import com.trustamarket.walletservice.wallet.application.dto.command.ChargePointCommand;
import com.trustamarket.walletservice.wallet.application.dto.result.ChargePointResult;
import com.trustamarket.walletservice.wallet.presentation.dto.request.ChargedPointRequest;
import com.trustamarket.walletservice.wallet.presentation.dto.response.ChargePointResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallets")
public class WalletController {
	private final WalletCommandService walletCommandService;

	@PostMapping("/charges")
	public CommonResponse<ChargePointResponse> chargePoint(@Valid @RequestBody ChargedPointRequest request){
		UUID userId = SecurityUtil.getCurrentUserIdOrThrow();
		UUID paymentId = UUID.randomUUID();

		ChargePointCommand command = new ChargePointCommand(userId, paymentId, request.chargeAmount());
		ChargePointResult result = walletCommandService.chargePoint(command);

		ChargePointResponse response = ChargePointResponse.from(result);

		return new CommonResponse(HttpStatus.OK.value(), response);
	}
}
