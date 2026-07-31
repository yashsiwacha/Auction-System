package com.auction.domain.exception;

public class AuctionNotFoundException extends DomainException {
    public AuctionNotFoundException(Long id) {
        super("Auction not found with ID: " + id);
    }
}
