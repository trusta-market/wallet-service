package com.trustamarket.walletservice.presentation;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trustamarket.walletservice.application.command.WalletCommandService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/wallets")
public class WalletInternalController {
	private final WalletCommandService walletCommandService;
	@PostMapping
	public ResponseEntity<Void> createWallet(UUID userId) { // return 타입과 파라미터 수정 필요

		walletCommandService.createWallet(userId);

		return new ResponseEntity<>(null);
	}
}
