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

import com.trustamarket.walletservice.wallet.application.command.PointTxRequestService;
import com.trustamarket.walletservice.wallet.application.command.SystemWalletProvider;
import com.trustamarket.walletservice.wallet.application.command.WalletCommandServiceImpl;
import com.trustamarket.walletservice.wallet.application.dto.command.UseWalletCommand;
import com.trustamarket.walletservice.wallet.application.dto.result.CreateWalletResult;
import com.trustamarket.walletservice.wallet.application.dto.result.UseWalletResult;
import com.trustamarket.walletservice.wallet.application.port.PaymentPort;
import com.trustamarket.walletservice.wallet.domain.entity.SystemWallet;
import com.trustamarket.walletservice.wallet.domain.entity.UserWallet;
import com.trustamarket.walletservice.wallet.domain.enums.PointTxType;
import com.trustamarket.walletservice.wallet.domain.enums.RefType;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRepository;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRequestHistoryRepository;
import com.trustamarket.walletservice.wallet.domain.repository.SystemWalletRepository;
import com.trustamarket.walletservice.wallet.domain.repository.UserWalletRepository;
import com.trustamarket.walletservice.wallet.global.handler.IdempotencyHandler;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WalletCommandServiceTest {

	@Mock private UserWalletRepository userWalletRepository;
	@Mock private SystemWalletRepository systemWalletRepository;
	@Mock private PointTransactionRepository pointTransactionRepository;
	@Mock private SystemWalletProvider systemWalletProvider;
	@Mock private PaymentPort paymentPort;
	@Mock private PointTransactionRequestHistoryRepository pointTxRequestHistoryRepository;
	@Mock private PointTxRequestService pointTxRequestService;
	@Mock private IdempotencyHandler idempotencyHandler;

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

			when(userWalletRepository.save(any(UserWallet.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

			// when
			walletCommandService.createUserWallet(userId);

			// then
			ArgumentCaptor<UserWallet> captor = ArgumentCaptor.forClass(UserWallet.class);
			verify(userWalletRepository).save(captor.capture());

			UserWallet savedWallet = captor.getValue();
			assertThat(savedWallet.getWalletOwner()).isEqualTo(userId);
			assertThat(savedWallet.checkBalance()).isZero();
		}

		@Test
		@DisplayName("이미 지갑이 존재하는 userId로 생성 시 이미 존재함을 반환한다")
		void createWallet_existingUser_returnsAlreadyExists() {
			// given
			UUID userId = UUID.randomUUID();

			when(userWalletRepository.existsByUserId(userId)).thenReturn(true);

			CreateWalletResult result = walletCommandService.createUserWallet(userId);

			assertThat(result.result()).isTrue();
			assertThat(result.walletId()).isNull();
		}
	}

	@Nested
	class UseWallet {
		@Test
		@DisplayName("잔액이 충분 -> 정상적으로 차감/적립")
		void usePoint_Success() {
			// given
			UUID buyerId = UUID.randomUUID();
			UUID orderId = UUID.randomUUID();
			long totalAmount = 1000L;
			UseWalletCommand command = new UseWalletCommand(orderId, buyerId, totalAmount);

			UserWallet buyerWallet = UserWallet.createUserWallet(buyerId);
			ReflectionTestUtils.setField(buyerWallet, "walletId", UUID.randomUUID());
			buyerWallet.increase(5000L, orderId, RefType.ORDER, PointTxType.CHARGE);

			SystemWallet escrowWallet = SystemWallet.createSystemWallet(UUID.randomUUID());
			ReflectionTestUtils.setField(escrowWallet, "walletId", UUID.randomUUID());

			when(userWalletRepository.findByUserId(buyerId)).thenReturn(Optional.of(buyerWallet));
			when(systemWalletProvider.getEscrowWallet()).thenReturn(escrowWallet);

			// when
			UseWalletResult result = walletCommandService.usePoint(command);

			// then
			verify(pointTransactionRepository, times(1)).saveAll(anyList());

			assertThat(result.balance()).isEqualTo(4000L);
			assertThat(buyerWallet.checkBalance()).isEqualTo(4000L);
			assertThat(escrowWallet.checkBalance()).isEqualTo(1000L);
		}

		@Test
		@DisplayName("지갑 잔액이 부족하면 결제가 진행되지 않고 부족한 금액(shortage)을 반환한다.")
		void usePoint_InsufficientBalance() {
			// given
			UUID buyerId = UUID.randomUUID();
			UUID orderId = UUID.randomUUID();
			long totalAmount = 5000L;
			UseWalletCommand command = new UseWalletCommand(orderId, buyerId, totalAmount);

			UserWallet buyerWallet = UserWallet.createUserWallet(buyerId);
			ReflectionTestUtils.setField(buyerWallet, "walletId", UUID.randomUUID());
			buyerWallet.increase(1000L, orderId, RefType.ORDER, PointTxType.CHARGE);

			when(userWalletRepository.findByUserId(buyerId)).thenReturn(Optional.of(buyerWallet));

			// when
			UseWalletResult result = walletCommandService.usePoint(command);

			// then
			verify(pointTransactionRepository, never()).saveAll(any());
			verify(systemWalletProvider, never()).getEscrowWallet();

			assertThat(buyerWallet.checkBalance()).isEqualTo(1000L);
			assertThat(result.shortage()).isEqualTo(4000L);
		}

		@Test
		@DisplayName("사용자의 지갑이 존재하지 않으면 WalletException을 던진다.")
		void usePoint_WalletNotFound() {
			UUID buyerId = UUID.randomUUID();
			UseWalletCommand command = new UseWalletCommand(UUID.randomUUID(), buyerId, 1000L);

			when(userWalletRepository.findByUserId(buyerId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> walletCommandService.usePoint(command))
				.isInstanceOf(WalletException.class);

			verify(systemWalletProvider, never()).getEscrowWallet();
			verify(pointTransactionRepository, never()).saveAll(any());
		}
	}
}
