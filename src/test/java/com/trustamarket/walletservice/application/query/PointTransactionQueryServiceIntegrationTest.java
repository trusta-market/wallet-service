package com.trustamarket.walletservice.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.trustamarket.walletservice.wallet.application.dto.query.GetPointTransactionQuery;
import com.trustamarket.walletservice.wallet.application.dto.result.GetPointTransactionPageResult;
import com.trustamarket.walletservice.wallet.application.query.WalletQueryService;
import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;
import com.trustamarket.walletservice.wallet.domain.entity.Wallet;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRepository;
import com.trustamarket.walletservice.wallet.domain.repository.WalletRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PointTransactionQueryServiceIntegrationTest {

	@Autowired
	private WalletQueryService walletQueryService;
	@Autowired
	private WalletRepository walletRepository;
	@Autowired
	private PointTransactionRepository pointTransactionRepository;
	@Autowired private EntityManager entityManager;

	/*
		현실적으로 같은 마이크로초에 거래가 발생하긴 어려우니
		연속된 짧은 시간에 발생한 거래들이 페이지 경계로 확인
		todo: controller test에서 확실히 데이터 넣어두고 test 추가 진행
	 */
	@Test
	@DisplayName("연속된 거래가 페이지 경계에 걸쳐도 중복 없이 조회 test")
	void consecutiveTransactions_noDuplicationAcrossPages() {
		UUID userId = UUID.randomUUID();
		Wallet wallet = walletRepository.save(Wallet.createUserWallet(userId));

		// 거래 5개 저장 (createdAt은 자동으로 현재 시각으로 찍힘)
		Set<UUID> createdIds = new HashSet<>();
		for (int i = 0; i < 5; i++) {
			PointTransaction tx = pointTransactionRepository.save(
				wallet.chargeComplete(1000L * (i + 1), UUID.randomUUID())
			);
			createdIds.add(tx.getPointTransactionId());
		}
		entityManager.flush();
		entityManager.clear();

		// 거래 시각이 현재 시각이니까 from/to도 현재 시각 기준
		Instant from = Instant.now().minusSeconds(3600);
		Instant to = Instant.now().plusSeconds(3600);

		Set<UUID> seen = new HashSet<>();
		Instant cursorTime = null;
		UUID cursorId = null;

		while (true) {
			GetPointTransactionPageResult page = walletQueryService.getPointTransactions(
				userId,
				GetPointTransactionQuery.of(from, to, cursorTime, cursorId, 2)
			);

			for (var tx : page.content()) {
				assertThat(seen).doesNotContain(tx.pointTxId())
					.as("페이지 간 중복 발생: " + tx.pointTxId());
				seen.add(tx.pointTxId());
			}

			if (!page.hasNext()) break;
			cursorTime = page.nextCursorTime();
			cursorId = page.nextCursorId();
		}

		assertThat(seen).containsExactlyInAnyOrderElementsOf(createdIds);
	}
}