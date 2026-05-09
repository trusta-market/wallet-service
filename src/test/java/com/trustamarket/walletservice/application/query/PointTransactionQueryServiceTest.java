package com.trustamarket.walletservice.application.query;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.trustamarket.walletservice.wallet.application.dto.query.GetPointTransactionQuery;
import com.trustamarket.walletservice.wallet.application.dto.result.GetPointTransactionPageResult;
import com.trustamarket.walletservice.wallet.application.query.WalletQueryServiceImpl;
import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;
import com.trustamarket.walletservice.wallet.domain.entity.Wallet;
import com.trustamarket.walletservice.wallet.domain.enums.PointTxType;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRepository;
import com.trustamarket.walletservice.wallet.domain.repository.WalletRepository;

@ExtendWith(MockitoExtension.class)
class PointTransactionQueryServiceTest {

	@Mock
	private WalletRepository walletRepository;

	@Mock
	private PointTransactionRepository pointTransactionRepository;

	@InjectMocks
	private WalletQueryServiceImpl queryService;

	@Test
	@DisplayName("첫 조회면 findFirstPointTransactions를 호출해 거래내역을 반환한다")
	void getPointTransactions_withoutCursor_callsFirstQuery() {
		UUID userId = UUID.randomUUID();
		Wallet wallet = Wallet.createUserWallet(userId);
		Instant now = Instant.now();

		PointTransaction tx1 = mockTransaction(UUID.randomUUID(), now, 3000L, PointTxType.CHARGE);
		PointTransaction tx2 = mockTransaction(UUID.randomUUID(), now.minusSeconds(60), 2000L, PointTxType.CHARGE);

		Slice<PointTransaction> slice = new SliceImpl<>(List.of(tx1, tx2), PageRequest.of(0, 2), true);

		when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
		when(pointTransactionRepository.findFirstPointTransactions(
			eq(wallet.getWalletId()), isNull(), isNull(), any()))
			.thenReturn(slice);

		GetPointTransactionPageResult result = queryService.getPointTransactions(
			userId,
			GetPointTransactionQuery.of(null, null, null, null, 2)
		);

		assertThat(result.content()).hasSize(2);
		assertThat(result.content().get(0).amount()).isEqualTo(3000L);
		assertThat(result.content().get(1).amount()).isEqualTo(2000L);
		assertThat(result.hasNext()).isTrue();
		assertThat(result.nextCursorId()).isEqualTo(tx2.getPointTransactionId());
		assertThat(result.nextCursorTime()).isEqualTo(tx2.getCreatedAt());

		verify(pointTransactionRepository, times(1))
			.findFirstPointTransactions(eq(wallet.getWalletId()), isNull(), isNull(), any());
		verify(pointTransactionRepository, never())
			.findNextPointTransactions(any(), any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("커서가 있으면 findNextPointTransactions를 호출한다")
	void getPointTransactions_withCursor_callsNextQuery() {
		UUID userId = UUID.randomUUID();
		Wallet wallet = Wallet.createUserWallet(userId);
		Instant cursorTime = Instant.now().minusSeconds(300);
		UUID cursorId = UUID.randomUUID();
		Instant from = Instant.now().minusSeconds(3600);
		Instant to = Instant.now();

		PointTransaction tx = mockTransaction(UUID.randomUUID(), Instant.now().minusSeconds(120), 1500L, PointTxType.SETTLEMENT_IN);
		Slice<PointTransaction> slice = new SliceImpl<>(List.of(tx), PageRequest.of(0, 1), false);

		when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
		when(pointTransactionRepository.findNextPointTransactions(
			eq(wallet.getWalletId()), eq(from), eq(to), eq(cursorTime), eq(cursorId), any()))
			.thenReturn(slice);

		GetPointTransactionPageResult result = queryService.getPointTransactions(
			userId,
			GetPointTransactionQuery.of(from, to, cursorTime, cursorId, 1)
		);

		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0).amount()).isEqualTo(1500L);
		assertThat(result.hasNext()).isFalse();
		assertThat(result.nextCursorId()).isNull();
		assertThat(result.nextCursorTime()).isNull();

		verify(pointTransactionRepository, times(1))
			.findNextPointTransactions(eq(wallet.getWalletId()), eq(from), eq(to), eq(cursorTime), eq(cursorId), any());
		verify(pointTransactionRepository, never())
			.findFirstPointTransactions(any(), any(), any(), any());
	}

	@Test
	@DisplayName("지갑이 없으면 WalletException을 던진다")
	void getPointTransactions_walletNotFound_throwsException() {
		UUID userId = UUID.randomUUID();
		when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> queryService.getPointTransactions(
			userId,
			GetPointTransactionQuery.of(null, null, null, null, 10)
		)).isInstanceOf(WalletException.class);

		verifyNoInteractions(pointTransactionRepository);
	}

	private PointTransaction mockTransaction(UUID id, Instant createdAt, long amount, PointTxType pointTxType) {
		PointTransaction tx = mock(PointTransaction.class);
		when(tx.getPointTransactionId()).thenReturn(id);
		when(tx.getCreatedAt()).thenReturn(createdAt);
		when(tx.getChangeAmount()).thenReturn(amount);
		when(tx.getPointTxType()).thenReturn(pointTxType);
		return tx;
	}
}
