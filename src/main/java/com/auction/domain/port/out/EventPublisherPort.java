package com.auction.domain.port.out;

import com.auction.domain.model.Bid;

public interface EventPublisherPort {
    void publishBidPlacedEvent(Bid bid);
    void publishAuctionEndedEvent(Long auctionId, Long winnerId);
}
