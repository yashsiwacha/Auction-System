package com.auction.domain.port.in;

import com.auction.domain.model.Auction;
import com.auction.domain.model.AuctionStatus;
import com.auction.domain.model.Bid;

import java.util.List;

public interface GetAuctionUseCase {
    Auction getAuctionById(Long id);
    List<Auction> getAuctionsByStatus(AuctionStatus status);
    List<Bid> getBidsForAuction(Long auctionId);
}
