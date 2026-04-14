package com.in.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class HighestBidResponse {

    private Integer productId;
    private BigDecimal currentHighestBid;
    private BigDecimal minimumNextBid;
    private Integer totalBidCount;
    private Integer highestBidId;
    private Integer highestBidderId;
    private String highestBidderName;
    private LocalDateTime highestBidTime;
    private Long productVersion;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public BigDecimal getCurrentHighestBid() {
        return currentHighestBid;
    }

    public void setCurrentHighestBid(BigDecimal currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
    }

    public BigDecimal getMinimumNextBid() {
        return minimumNextBid;
    }

    public void setMinimumNextBid(BigDecimal minimumNextBid) {
        this.minimumNextBid = minimumNextBid;
    }

    public Integer getTotalBidCount() {
        return totalBidCount;
    }

    public void setTotalBidCount(Integer totalBidCount) {
        this.totalBidCount = totalBidCount;
    }

    public Integer getHighestBidId() {
        return highestBidId;
    }

    public void setHighestBidId(Integer highestBidId) {
        this.highestBidId = highestBidId;
    }

    public Integer getHighestBidderId() {
        return highestBidderId;
    }

    public void setHighestBidderId(Integer highestBidderId) {
        this.highestBidderId = highestBidderId;
    }

    public String getHighestBidderName() {
        return highestBidderName;
    }

    public void setHighestBidderName(String highestBidderName) {
        this.highestBidderName = highestBidderName;
    }

    public LocalDateTime getHighestBidTime() {
        return highestBidTime;
    }

    public void setHighestBidTime(LocalDateTime highestBidTime) {
        this.highestBidTime = highestBidTime;
    }

    public Long getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(Long productVersion) {
        this.productVersion = productVersion;
    }
}
