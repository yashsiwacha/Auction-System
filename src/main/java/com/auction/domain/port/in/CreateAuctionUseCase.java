package com.auction.domain.port.in;

import com.auction.domain.model.Auction;

import java.math.BigDecimal;
import java.time.Instant;

public interface CreateAuctionUseCase {
    Auction createAuction(String title, String description, String category, BigDecimal startingPrice, Long sellerId, Instant startTime, Instant endTime);
}
