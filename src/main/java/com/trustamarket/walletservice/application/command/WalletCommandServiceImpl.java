package com.trustamarket.walletservice.application.command;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trustamarket.walletservice.application.dto.result.CreateWalletResult;
import com.trustamarket.walletservice.domain.entity.Wallet;
import com.trustamarket.walletservice.domain.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletCommandServiceImpl implements WalletCommandService{

	private final WalletRepository walletRepository;

	@Transactional
	public CreateWalletResult createWallet(UUID userId) {
		if (userId == null) {
			throw new IllegalArgumentException("사용자 ID는 필수입니다");
		}
		try {
			Wallet wallet = Wallet.create(userId);
			walletRepository.save(wallet);
			return new CreateWalletResult(wallet.getWalletId());
		} catch (DataIntegrityViolationException e) {
			throw new RuntimeException(); // business exception으로 변경 예정
		}
	}
}
