package com.trusta_market.settlement_service.application.command;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.trustamarket.walletservice.application.command.WalletCommandServiceImpl;
import com.trustamarket.walletservice.domain.entity.Wallet;
import com.trustamarket.walletservice.domain.repository.WalletRepository;

@ExtendWith(MockitoExtension.class)
class WalletCommandServiceTest {

	@Mock
	private WalletRepository walletRepository;

	@InjectMocks
	private WalletCommandServiceImpl walletCommandService;

	@Nested
	@DisplayName("지갑 생성")
	class CreateWallet {

		@Test
		@DisplayName("입력받은 userId로 지갑이 저장된다")
		void createWallet_savesWithCorrectUserId() {
			// given
			UUID userId = UUID.randomUUID();

			when(walletRepository.save(any(Wallet.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

			// when
			walletCommandService.createWallet(userId);

			// then
			ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
			verify(walletRepository).save(captor.capture());

			Wallet savedWallet = captor.getValue();
			assertThat(savedWallet.getWalletOwner()).isEqualTo(userId);
			assertThat(savedWallet.checkBalance()).isZero();
		}

		@Test
		@DisplayName("동시성으로 DB 제약 위반 시 WalletAlreadyExistsException으로 변환된다")
		void createWallet_dataIntegrityViolation_throwsWalletAlreadyExistsException() {
			// given
			UUID userId = UUID.randomUUID();

			when(walletRepository.save(any(Wallet.class)))
				.thenThrow(new DataIntegrityViolationException("Duplicate key"));

			// when & then
			assertThatThrownBy(() -> walletCommandService.createWallet(userId))
				.isInstanceOf(RuntimeException.class);
		}
	}
}
