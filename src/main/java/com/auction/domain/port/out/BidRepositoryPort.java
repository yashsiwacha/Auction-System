package com.auction.domain.port.out;

import com.auction.domain.model.Bid;

import java.util.List;
import java.util.Optional;

public interface BidRepositoryPort {
    Bid save(Bid bid);
    List<Bid> findByAuctionIdOrderByAmountDesc(Long auctionId);
    Optional<Bid> findByIdempotencyKey(String idempotencyKey);
}
