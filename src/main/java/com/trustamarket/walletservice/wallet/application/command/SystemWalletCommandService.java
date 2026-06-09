package com.trustamarket.walletservice.wallet.application.command;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trustamarket.walletservice.wallet.application.creator.SystemWalletCreator;
import com.trustamarket.walletservice.wallet.application.dto.creator.CreateSystemWalletDto;
import com.trustamarket.walletservice.wallet.domain.entity.SystemWallet;
import com.trustamarket.walletservice.wallet.domain.enums.SystemWalletType;
import com.trustamarket.walletservice.wallet.domain.repository.SystemWalletRepository;

@Service
public class SystemWalletCommandService {

	private final Map<SystemWalletType, SystemWalletCreator> creators;
	private final SystemWalletRepository systemWalletRepository;


	public SystemWalletCommandService(List<SystemWalletCreator> creatorList,
		SystemWalletRepository systemWalletRepository) {
		this.creators = creatorList.stream()
			.collect(Collectors.toMap(
				creator -> creator.getType(),
				creator -> creator
			));
		this.systemWalletRepository = systemWalletRepository;
	}

	@Transactional
	public SystemWallet createSystemWallet(CreateSystemWalletDto systemWalletDto) {
		SystemWalletType type = systemWalletDto.systemWalletType();
		UUID operatorId = systemWalletDto.operatorId();

		SystemWalletCreator creator = creators.get(type);
		if (creator == null) {
			throw new IllegalArgumentException("지원하지 않는 시스템 지갑 타입");
		}

		SystemWallet systemWallet = creator.create(operatorId);
		SystemWallet saved = systemWalletRepository.save(systemWallet);
		return saved;
	}
}