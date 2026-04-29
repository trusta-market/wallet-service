package com.trustamarket.walletservice.application.command;

import static com.trustamarket.walletservice.domain.exception.WalletErrorCode.*;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trustamarket.walletservice.application.dto.result.CreateWalletResult;
import com.trustamarket.walletservice.domain.entity.Wallet;
import com.trustamarket.walletservice.domain.exception.WalletException;
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

		if (walletRepository.existsByUserId(userId)) {
			throw new WalletException(ALREADY_EXISTS_WALLET);
		}

		Wallet wallet = Wallet.createUserWallet(userId);
		walletRepository.save(wallet); //DataIntegrity exception은 RestControllerAdvice에서 처리
		return new CreateWalletResult(wallet.getWalletId());
	}
}
