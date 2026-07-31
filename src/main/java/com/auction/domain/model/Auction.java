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
public class Auction {
    private Long id;
    private String title;
    private String description;
    private String category;
    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private Long sellerId;
    private Long winnerId;
    private AuctionStatus status;
    private Long version;
    private Instant startTime;
    private Instant endTime;
    private Instant createdAt;
    private Instant updatedAt;

    public boolean isExpired() {
        return Instant.now().isAfter(endTime);
    }

    public boolean isBiddable() {
        return status == AuctionStatus.ACTIVE && !isExpired();
    }
}
