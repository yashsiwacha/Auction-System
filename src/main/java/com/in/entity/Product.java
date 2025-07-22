package com.in.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer id;
    
    @Column(name = "name", nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "starting_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal startingPrice;
    
    @Column(name = "current_highest_bid", precision = 19, scale = 2)
    private BigDecimal currentHighestBid;
    
    @Column(name = "reserve_price", precision = 19, scale = 2)
    private BigDecimal reservePrice;
    
    @Column(name = "auction_start_time", nullable = false)
    private LocalDateTime auctionStartTime;
    
    @Column(name = "auction_end_time", nullable = false)
    private LocalDateTime auctionEndTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.PENDING;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Many products belong to one seller
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
    
    // One product can have many bids
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bid> bids;
    
    // Winner of the auction (nullable until auction ends)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;
    
    // Auction slot relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_slot_id")
    private AuctionSlot auctionSlot;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (currentHighestBid == null) {
            currentHighestBid = startingPrice;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum ProductStatus {
        PENDING,     // Waiting for admin approval
        APPROVED,    // Approved and active for bidding
        REJECTED,    // Rejected by admin
        COMPLETED,   // Auction ended
        CANCELLED    // Cancelled by seller or admin
    }
    
    public boolean isAuctionActive() {
        LocalDateTime now = LocalDateTime.now();
        if (status != ProductStatus.APPROVED) {
            return false;
        }
        
        // Check if auction slot is active
        if (auctionSlot != null) {
            LocalDate today = now.toLocalDate();
            LocalTime currentTime = now.toLocalTime();
            
            return auctionSlot.getSlotDate().equals(today) &&
                   currentTime.isAfter(auctionSlot.getStartTime()) &&
                   currentTime.isBefore(auctionSlot.getEndTime()) &&
                   auctionSlot.getIsActive();
        }
        
        // Fallback to original time-based check
        return now.isAfter(auctionStartTime) && now.isBefore(auctionEndTime);
    }
    
    public boolean isAuctionEnded() {
        return LocalDateTime.now().isAfter(auctionEndTime);
    }
    
    // Constructors
    public Product() {}
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getStartingPrice() { return startingPrice; }
    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }
    
    public BigDecimal getCurrentHighestBid() { return currentHighestBid; }
    public void setCurrentHighestBid(BigDecimal currentHighestBid) { this.currentHighestBid = currentHighestBid; }
    
    public BigDecimal getReservePrice() { return reservePrice; }
    public void setReservePrice(BigDecimal reservePrice) { this.reservePrice = reservePrice; }
    
    public LocalDateTime getAuctionStartTime() { return auctionStartTime; }
    public void setAuctionStartTime(LocalDateTime auctionStartTime) { this.auctionStartTime = auctionStartTime; }
    
    public LocalDateTime getAuctionEndTime() { return auctionEndTime; }
    public void setAuctionEndTime(LocalDateTime auctionEndTime) { this.auctionEndTime = auctionEndTime; }
    
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }
    
    public List<Bid> getBids() { return bids; }
    public void setBids(List<Bid> bids) { this.bids = bids; }
    
    public User getWinner() { return winner; }
    public void setWinner(User winner) { this.winner = winner; }
    
    public AuctionSlot getAuctionSlot() { return auctionSlot; }
    public void setAuctionSlot(AuctionSlot auctionSlot) { this.auctionSlot = auctionSlot; }
    
    /**
     * Check if product can be deleted (only before auction slot starts)
     */
    public boolean canBeDeleted() {
        if (auctionSlot == null) {
            // If no slot assigned, can delete if not completed
            return status != ProductStatus.COMPLETED;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        
        // Can delete if slot hasn't started yet
        if (auctionSlot.getSlotDate().isAfter(today)) {
            return true; // Future date
        } else if (auctionSlot.getSlotDate().equals(today)) {
            return currentTime.isBefore(auctionSlot.getStartTime()); // Same day, before start time
        } else {
            return false; // Past date
        }
    }
}
