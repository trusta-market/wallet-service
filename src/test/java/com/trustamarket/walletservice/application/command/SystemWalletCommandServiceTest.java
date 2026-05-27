package com.trustamarket.walletservice.application.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.trustamarket.walletservice.wallet.application.command.SystemWalletCommandService;
import com.trustamarket.walletservice.wallet.application.creator.SystemWalletCreator;
import com.trustamarket.walletservice.wallet.application.dto.creator.CreateSystemWalletDto;
import com.trustamarket.walletservice.wallet.domain.entity.SystemWallet;
import com.trustamarket.walletservice.wallet.domain.enums.SystemWalletType;
import com.trustamarket.walletservice.wallet.domain.repository.SystemWalletRepository;

@ExtendWith(MockitoExtension.class)
class SystemWalletCommandServiceTest {

	@Mock
	private SystemWalletRepository walletRepository;

	@Mock
	private SystemWalletCreator escrowCreator;

	@Mock
	private SystemWalletCreator feeCreator;

	private SystemWalletCommandService systemWalletService;

	@BeforeEach
	void setUp() {
		when(escrowCreator.getType()).thenReturn(SystemWalletType.SYSTEM_ESCROW);
		when(feeCreator.getType()).thenReturn(SystemWalletType.SYSTEM_FEE);

		systemWalletService = new SystemWalletCommandService(
			List.of(escrowCreator, feeCreator),
			walletRepository
		);
	}

	@Test
	@DisplayName("SYSTEM_ESCROW")
	void createSystemEscrowWallet_success() {
		// given
		UUID operatorId = UUID.randomUUID();
		CreateSystemWalletDto dto = CreateSystemWalletDto.of(
			operatorId, SystemWalletType.SYSTEM_ESCROW
		);
		SystemWallet expectedWallet = SystemWallet.createSystemWallet(operatorId);

		given(escrowCreator.create(operatorId)).willReturn(expectedWallet);
		given(walletRepository.save(expectedWallet)).willReturn(expectedWallet);

		// when
		SystemWallet result = systemWalletService.createSystemWallet(dto);

		// then
		assertThat(result).isEqualTo(expectedWallet);
		verify(escrowCreator).create(operatorId);
		verify(feeCreator, never()).create(any());
		verify(walletRepository).save(expectedWallet);
	}

	@Test
	@DisplayName("SYSTEM_FEE")
	void createSystemFeeWallet_success() {
		// given
		UUID operatorId = UUID.randomUUID();
		CreateSystemWalletDto dto = CreateSystemWalletDto.of(
			operatorId, SystemWalletType.SYSTEM_FEE
		);
		SystemWallet expectedWallet = SystemWallet.createSystemFeeWallet(operatorId);

		given(feeCreator.create(operatorId)).willReturn(expectedWallet);
		given(walletRepository.save(expectedWallet)).willReturn(expectedWallet);

		// when
		SystemWallet result = systemWalletService.createSystemWallet(dto);

		// then
		assertThat(result).isEqualTo(expectedWallet);
		verify(feeCreator).create(operatorId);
		verify(escrowCreator, never()).create(any());
	}
}
