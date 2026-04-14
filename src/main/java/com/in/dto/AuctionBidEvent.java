package com.in.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionBidEvent {

    private Integer bidId;
    private Integer productId;
    private Integer bidderId;
    private String bidderName;
    private BigDecimal bidAmount;
    private BigDecimal previousHighestBid;
    private BigDecimal currentHighestBid;
    private Long totalBidCount;
    private Long productVersion;
    private LocalDateTime bidTime;

    public Integer getBidId() {
        return bidId;
    }

    public void setBidId(Integer bidId) {
        this.bidId = bidId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getBidderId() {
        return bidderId;
    }

    public void setBidderId(Integer bidderId) {
        this.bidderId = bidderId;
    }

    public String getBidderName() {
        return bidderName;
    }

    public void setBidderName(String bidderName) {
        this.bidderName = bidderName;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }

    public BigDecimal getPreviousHighestBid() {
        return previousHighestBid;
    }

    public void setPreviousHighestBid(BigDecimal previousHighestBid) {
        this.previousHighestBid = previousHighestBid;
    }

    public BigDecimal getCurrentHighestBid() {
        return currentHighestBid;
    }

    public void setCurrentHighestBid(BigDecimal currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
    }

    public Long getTotalBidCount() {
        return totalBidCount;
    }

    public void setTotalBidCount(Long totalBidCount) {
        this.totalBidCount = totalBidCount;
    }

    public Long getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(Long productVersion) {
        this.productVersion = productVersion;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    public void setBidTime(LocalDateTime bidTime) {
        this.bidTime = bidTime;
    }
}
