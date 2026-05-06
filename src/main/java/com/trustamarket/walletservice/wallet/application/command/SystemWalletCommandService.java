package com.trustamarket.walletservice.wallet.application.command;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trustamarket.walletservice.wallet.application.creator.SystemWalletCreator;
import com.trustamarket.walletservice.wallet.application.dto.creator.CreateSystemWalletDto;
import com.trustamarket.walletservice.wallet.domain.entity.Wallet;
import com.trustamarket.walletservice.wallet.domain.enums.WalletType;
import com.trustamarket.walletservice.wallet.domain.repository.WalletRepository;

@Service
public class SystemWalletCommandService {

	private final Map<WalletType, SystemWalletCreator> creators;
	private final WalletRepository walletRepository;


	public SystemWalletCommandService(List<SystemWalletCreator> creatorList,
		WalletRepository walletRepository) {
		this.creators = creatorList.stream()
			.collect(Collectors.toMap(
				creator -> creator.getType(),
				creator -> creator
			));
		this.walletRepository = walletRepository;
	}

	@Transactional
	public Wallet createSystemWallet(CreateSystemWalletDto systemWalletDto) {
		WalletType type = systemWalletDto.walletType();
		UUID operatorId = systemWalletDto.operatorId();

		SystemWalletCreator creator = creators.get(type);
		if (creator == null) {
			throw new IllegalArgumentException("지원하지 않는 시스템 지갑 타입");
		}

		Wallet wallet = creator.create(operatorId);
		Wallet saved = walletRepository.save(wallet);
		return saved;
	}
}