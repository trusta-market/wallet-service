package com.trusta_market.walllet_service.domain.vo;

import java.util.UUID;

import com.trusta_market.walllet_service.domain.enums.RefType;

import jakarta.persistence.Embeddable;

@Embeddable
public record Reference(
	UUID refId,
	RefType refType
) {
}
