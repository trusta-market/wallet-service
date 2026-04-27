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

import com.trustamarket.walletservice.application.command.WalletCommandServiceImpl;
import com.trustamarket.walletservice.domain.entity.Wallet;
import com.trustamarket.walletservice.domain.exception.WalletErrorCode;
import com.trustamarket.walletservice.domain.exception.WalletException;
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
		@DisplayName("이미 지갑이 존재하는 userId로 생성 시 WalletException이 발생한다")
		void createWallet_existingUser_throwsWalletException() {
			// given
			UUID userId = UUID.randomUUID();

			when(walletRepository.existsByUserId(userId)).thenReturn(true);

			assertThatThrownBy(() -> walletCommandService.createWallet(userId))
				.isInstanceOf(WalletException.class)
				.extracting("errorCode")
				.isEqualTo(WalletErrorCode.ALREADY_EXISTS_WALLET);
		}
	}
}
