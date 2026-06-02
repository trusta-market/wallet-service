package com.trustamarket.walletservice.wallet.infrastructure.outbox;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.trustamarket.walletservice.wallet.domain.entity.SystemWallet;
import com.trustamarket.walletservice.wallet.domain.entity.SystemWalletOutbox;
import com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;
import com.trustamarket.walletservice.wallet.domain.repository.SystemWalletOutboxRepository;
import com.trustamarket.walletservice.wallet.domain.repository.SystemWalletRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class SystemWalletOutboxProcessor {
	private static final int MAX_RETRY = 5;

	private final SystemWalletRepository systemWalletRepository;
	private final SystemWalletOutboxRepository systemWalletOutboxRepository;

	@Transactional
	public void apply(UUID outboxId) {
		SystemWalletOutbox outbox = systemWalletOutboxRepository.findById(outboxId)
			.orElseThrow(() -> new IllegalArgumentException("outbox not found: " + outboxId));

		int retryCount = outbox.increaseRetrycount();
		if (retryCount > MAX_RETRY) {
			outbox.markFailed();
			log.error("system wallet outbox max retry exceeded: outboxId={}", outbox.getOutboxId());
			return;
			//  exception을 던지면 트랜잭션이 롤백되면서 markFailed()와 increaseRetrycount() 변경사항도 함께 취소
			//  그러면 outbox가 PENDING 상태로 남아서 recover()가 계속 올림
		}

		SystemWallet systemWallet = systemWalletRepository.findById(outbox.getWalletId())
			.orElseThrow(
				() -> new WalletException("walletId = " + outbox.getWalletId(), WalletErrorCode.WALLET_NOT_FOUND));

		systemWallet.applyBalanceDelta(outbox.getDeltaAmount());
		systemWalletRepository.save(systemWallet);
		outbox.markApplied();
	}


}
