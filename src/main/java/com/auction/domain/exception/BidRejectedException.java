package com.auction.domain.exception;

public class BidRejectedException extends DomainException {
    public BidRejectedException(String message) {
        super(message);
    }
}
