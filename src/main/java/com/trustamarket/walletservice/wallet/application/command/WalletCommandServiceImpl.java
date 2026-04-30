package com.trustamarket.walletservice.wallet.application.command;

import static com.trustamarket.walletservice.wallet.domain.exception.WalletErrorCode.*;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trustamarket.walletservice.wallet.application.dto.result.CreateWalletResult;
import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;
import com.trustamarket.walletservice.wallet.domain.entity.Wallet;
import com.trustamarket.walletservice.wallet.domain.exception.WalletException;
import com.trustamarket.walletservice.wallet.domain.repository.PointTransactionRepository;
import com.trustamarket.walletservice.wallet.domain.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletCommandServiceImpl implements WalletCommandService {

	private final WalletRepository walletRepository;
	private final SystemWalletProvider systemWalletProvider;

	private final PointTransactionRepository pointTransactionRepository;

	@Transactional
	public CreateWalletResult createWallet(UUID userId) {
		if (userId == null) {
			throw new IllegalArgumentException("사용자 ID는 필수입니다");
		}

		if (walletRepository.existsByUserId(userId)) {
			throw new WalletException(ALREADY_EXISTS_WALLET);
		}

		Wallet wallet = Wallet.createUserWallet(userId);
		walletRepository.save(wallet); //DataIntegrity exception은 RestControllerAdvice에서 처리
		return new CreateWalletResult(wallet.getWalletId());
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY) // 부모 트랜잭션(정산)에 반드시 합류하도록 설정
	public void transferForSettlement(UUID orderId, UUID sellerId, long totalAmount, long sellerAmount, long feeAmount) {

		// 1. 지갑 조회 (지갑 도메인의 책임)
		Wallet adminWallet = systemWalletProvider.getEscrowWallet();
		Wallet feeWallet = systemWalletProvider.getFeeWallet();
		Wallet sellerWallet = walletRepository.findByUserId(sellerId)
			.orElseThrow(() -> new WalletException(WALLET_NOT_FOUND));

		// 2. 포인트 이동 로직 (지갑 도메인의 책임)
		PointTransaction adminTx = adminWallet.settleOut(totalAmount, orderId);
		PointTransaction sellerTx = sellerWallet.settleIn(sellerAmount, orderId);
		PointTransaction feeTx = feeWallet.increaseFeeRevenue(feeAmount, orderId);

		// 3. DB 저장 (지갑 도메인의 책임)
		pointTransactionRepository.saveAll(List.of(adminTx, sellerTx, feeTx));
	}
}
