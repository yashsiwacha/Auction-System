package com.auction.domain.port.in;

import com.auction.domain.model.Bid;

import java.math.BigDecimal;

public interface PlaceBidUseCase {
    Bid placeBid(Long auctionId, Long bidderId, BigDecimal amount, String idempotencyKey);
}
