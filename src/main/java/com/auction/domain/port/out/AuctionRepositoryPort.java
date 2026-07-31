package com.auction.domain.port.out;

import com.auction.domain.model.Auction;
import com.auction.domain.model.AuctionStatus;

import java.util.List;
import java.util.Optional;

public interface AuctionRepositoryPort {
    Auction save(Auction auction);
    Optional<Auction> findById(Long id);
    List<Auction> findAllByStatus(AuctionStatus status);
}
