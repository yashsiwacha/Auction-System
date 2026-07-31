package com.auction.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bid {
    private Long id;
    private Long auctionId;
    private Long bidderId;
    private BigDecimal amount;
    private BidStatus status;
    private String idempotencyKey;
    private Instant timestamp;
}
