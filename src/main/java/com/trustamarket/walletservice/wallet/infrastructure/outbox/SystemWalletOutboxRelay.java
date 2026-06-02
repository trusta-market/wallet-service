package com.trustamarket.walletservice.wallet.infrastructure.outbox;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.trustamarket.walletservice.wallet.application.dto.event.SystemWalletOutboxEvent;
import com.trustamarket.walletservice.wallet.domain.repository.SystemWalletOutboxRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// outbox 패턴에서 DB를 읽는 쪽
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemWalletOutboxRelay {
	private LinkedBlockingQueue<UUID> blockingQueue = new LinkedBlockingQueue<>();
	private final SystemWalletOutboxProcessor processor;
	private final SystemWalletOutboxRepository systemWalletOutboxRepository;

	@PostConstruct
	public void consume() {
		Thread consumer = new Thread(() -> {
			while(!Thread.currentThread().isInterrupted()) {
				try {
					UUID outboxId = blockingQueue.take(); // 비어있으면 OS가 스레드를 재움, CPU 0%
					processor.apply(outboxId);
				} catch (InterruptedException interruptedException) {
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					log.error("outbox processing failed", e);
				}
			}
		}, "system-wallet-balance-outbox-consumer");
		consumer.setDaemon(true); // main 죽으면 해당 thread도 종료
		consumer.start();
	}

	// 커밋 직후 큐에 넣기
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(SystemWalletOutboxEvent systemWalletOutboxEvent) {
		blockingQueue.offer(systemWalletOutboxEvent.outboxId());
	}


	// 처리 못 한 것들 재투입 (서버 재시작, handle 실패 등)
	@Scheduled(fixedDelay = 5000)
	public void recover() {
		systemWalletOutboxRepository.findAllByOutboxStatusIsPending(Instant.now().minusSeconds(10))
			.forEach(outbox -> blockingQueue.offer(outbox.getOutboxId()));
	}
}
