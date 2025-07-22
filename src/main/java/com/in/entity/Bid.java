package com.in.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
public class Bid {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bid_id")
    private Integer id;
    
    @Column(name = "bid_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal bidAmount;
    
    @Column(name = "bid_time", nullable = false)
    private LocalDateTime bidTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BidStatus status = BidStatus.ACTIVE;
    
    // Many bids belong to one bidder (user)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User bidder;
    
    // Many bids belong to one product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Product product;
    
    @PrePersist
    protected void onCreate() {
        bidTime = LocalDateTime.now();
    }
    
    public enum BidStatus {
        ACTIVE,      // Current valid bid
        OUTBID,      // Bid has been outbid by someone else
        WINNING,     // Currently the highest bid
        WON,         // Won the auction
        CANCELLED    // Bid cancelled by admin
    }
    
    // Constructors
    public Bid() {}
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public BigDecimal getBidAmount() { return bidAmount; }
    public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }
    
    public LocalDateTime getBidTime() { return bidTime; }
    public void setBidTime(LocalDateTime bidTime) { this.bidTime = bidTime; }
    
    public BidStatus getStatus() { return status; }
    public void setStatus(BidStatus status) { this.status = status; }
    
    public User getBidder() { return bidder; }
    public void setBidder(User bidder) { this.bidder = bidder; }
    
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}
