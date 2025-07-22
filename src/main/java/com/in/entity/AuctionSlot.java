package com.in.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "auction_slots")
public class AuctionSlot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slot_id")
    private Integer id;
    
    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "max_products", nullable = false)
    private Integer maxProducts = 3;
    
    @Column(name = "current_products", nullable = false)
    private Integer currentProducts = 0;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "auctionSlot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;
    
    // Constructors
    public AuctionSlot() {}
    
    public AuctionSlot(LocalDate slotDate, LocalTime startTime, LocalTime endTime) {
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public LocalDate getSlotDate() { return slotDate; }
    public void setSlotDate(LocalDate slotDate) { this.slotDate = slotDate; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    
    public Integer getMaxProducts() { return maxProducts; }
    public void setMaxProducts(Integer maxProducts) { this.maxProducts = maxProducts; }
    
    public Integer getCurrentProducts() { return currentProducts; }
    public void setCurrentProducts(Integer currentProducts) { this.currentProducts = currentProducts; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }
    
    // Helper methods
    public boolean hasAvailableSlots() {
        return currentProducts < maxProducts;
    }
    
    public void incrementProductCount() {
        this.currentProducts++;
    }
    
    public void decrementProductCount() {
        if (this.currentProducts > 0) {
            this.currentProducts--;
        }
    }
    
    public String getTimeRange() {
        return startTime + " - " + endTime;
    }
}
