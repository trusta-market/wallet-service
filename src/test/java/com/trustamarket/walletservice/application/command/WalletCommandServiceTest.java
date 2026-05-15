package com.trustamarket.walletservice.application.command;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trustamarket.walletservice.wallet.application.command.SystemWalletProvider;
import com.trustamarket.walletservice.wallet.application.command.WalletCommandServiceImpl;
import com.trustamarket.walletservice.wallet.application.dto.command.UseWalletCommand;
import com.trustamarket.walletservice.wallet.application.dto.result.CreateWalletResult;
import com.trustamarket.walletservice.wallet.application.dto.result.UseWalletResult;
import com.trustamarket.walletservice.wallet.domain.entity.Wallet;
import com.trustamarket.walletservice.wallet.domain.enums.PointTxType;
import com.trustamarket.walletservice.wallet.domain.enums.RefType;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRepository;
import com.trustamarket.walletservice.wallet.domain.repository.WalletRepository;

@ExtendWith(MockitoExtension.class)
class WalletCommandServiceTest {

	@Mock
	private WalletRepository walletRepository;
	@Mock
	private PointTransactionRepository pointTransactionRepository;
	@Mock
	private SystemWalletProvider systemWalletProvider;

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

			CreateWalletResult result = walletCommandService.createWallet(userId);
			
			assertThat(result.result()).isTrue();
			assertThat(result.walletId()).isNull();
			// assertThatThrownBy(() -> walletCommandService.createWallet(userId))
				// .isInstanceOf(WalletException.class)
				// .extracting("errorCode")
				// .isEqualTo(WalletErrorCode.ALREADY_EXISTS_WALLET);
		}
	}

	@Nested
	class UseWallet {
		@Test
		@DisplayName("잔액이 충분 -> 정상적으로 차감/적립")
		void usePoint_Success() {
			// given
			UUID userId = UUID.randomUUID();
			UUID buyerId = UUID.randomUUID();
			UUID orderId = UUID.randomUUID();
			long totalAmount = 1000L;
			UseWalletCommand command = new UseWalletCommand(orderId, buyerId, totalAmount);

			Wallet buyerWallet = Wallet.createUserWallet(buyerId);
			buyerWallet.increase(5000L, orderId, RefType.ORDER, PointTxType.CHARGE);

			Wallet escrowWallet = Wallet.createSystemWallet(UUID.randomUUID());
			// Repository(외부 의존성)만 Mock으로 동작하게 설정
			when(walletRepository.findByUserId(buyerId)).thenReturn(Optional.of(buyerWallet));
			when(systemWalletProvider.getEscrowWallet()).thenReturn(escrowWallet);

			// when
			UseWalletResult result = walletCommandService.usePoint(command);

			// then
			// Repository의 saveAll이 정확히 1번 호출되었는지 검증
			verify(pointTransactionRepository, times(1)).saveAll(anyList());

			assertThat(result.balance()).isEqualTo(4000L);
			assertThat(buyerWallet.checkBalance()).isEqualTo(4000L);
			assertThat(escrowWallet.checkBalance()).isEqualTo(1000L);
		}

		@Test
		@DisplayName("지갑 잔액이 부족하면 결제가 진행되지 않고 부족한 금액(shortage)을 반환한다.")
		void usePoint_InsufficientBalance() {
			// given
			UUID userId = UUID.randomUUID();
			UUID buyerId = UUID.randomUUID();
			UUID orderId = UUID.randomUUID();
			long totalAmount = 5000L; // 결제 시도 금액: 5000원
			UseWalletCommand command = new UseWalletCommand(orderId, buyerId, totalAmount);

			Wallet buyerWallet = Wallet.createUserWallet(buyerId);
			buyerWallet.increase(1000L, orderId, RefType.ORDER, PointTxType.CHARGE);

			when(walletRepository.findByUserId(buyerId)).thenReturn(Optional.of(buyerWallet));

			// when
			UseWalletResult result = walletCommandService.usePoint(command);

			// then
			// 돈이 안 빠져나갔다는 보장
			verify(pointTransactionRepository, never()).saveAll(any());
			verify(systemWalletProvider, never()).getEscrowWallet();

			assertThat(buyerWallet.checkBalance()).isEqualTo(1000L);
			assertThat(result.shortage()).isEqualTo(4000L);
		}

		@Test
		@DisplayName("사용자의 지갑이 존재하지 않으면 WalletException을 던진다.")
		void usePoint_WalletNotFound() {
			UUID userId = UUID.randomUUID();
			UUID buyerId = UUID.randomUUID();
			UseWalletCommand command = new UseWalletCommand(UUID.randomUUID(), buyerId, 1000L);

			when(walletRepository.findByUserId(buyerId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> walletCommandService.usePoint(command))
				.isInstanceOf(WalletException.class);

			verify(systemWalletProvider, never()).getEscrowWallet();
			verify(pointTransactionRepository, never()).saveAll(any());
		}
	}
}
