package com.in.dto;

import java.math.BigDecimal;

public class PlaceBidRequest {

    private Integer bidderId;
    private BigDecimal bidAmount;

    public Integer getBidderId() {
        return bidderId;
    }

    public void setBidderId(Integer bidderId) {
        this.bidderId = bidderId;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }
}
