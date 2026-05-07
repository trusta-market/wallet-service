package com.trustamarket.walletservice.wallet.application.dto.result;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Slice;

import com.trustamarket.walletservice.wallet.domain.entity.PointTransaction;
import com.trustamarket.walletservice.wallet.domain.enums.PointTxType;

public record GetPointTransactionPageResult(
	List<GetPointTransactionResponse> content,
	boolean hasNext,
	Instant nextCursorTime,
	UUID nextCursorId
) {
	public static GetPointTransactionPageResult from(Slice<PointTransaction> slice) {
		List<GetPointTransactionResponse> content = slice.getContent().stream()
			.map(GetPointTransactionResponse::from)
			.toList();

		Instant nextCursorTime = null;
		UUID nextCursorId = null;

		if (slice.hasNext() && !slice.getContent().isEmpty()) {
			PointTransaction last = slice.getContent()
				.get(slice.getContent().size() - 1);
			nextCursorTime = last.getCreatedAt();
			nextCursorId = last.getPointTransactionId();
		}

		return new GetPointTransactionPageResult(
			content, slice.hasNext(), nextCursorTime, nextCursorId
		);
	}


	public record GetPointTransactionResponse (
		UUID pointTxId,
		long amount,
		PointTxType pointTxType,
		Instant createdAt
	) {
		public static GetPointTransactionResponse from(PointTransaction pointTransaction) {
			return new GetPointTransactionResponse(
				pointTransaction.getPointTransactionId(),
				pointTransaction.getUsePoint(),
				pointTransaction.getPointTxType(),
				pointTransaction.getCreatedAt()
			);
		}
	}
}
