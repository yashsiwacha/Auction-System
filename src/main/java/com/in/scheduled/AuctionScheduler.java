package com.in.scheduled;

import com.in.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuctionScheduler {
    
    @Autowired
    private ProductService productService;
    
    // Run every minute to check for expired auctions
    @Scheduled(fixedRate = 60000) // 60 seconds = 60000 milliseconds
    public void updateExpiredAuctions() {
        try {
            productService.updateExpiredAuctions();
        } catch (Exception e) {
            System.err.println("Error updating expired auctions: " + e.getMessage());
        }
    }
    
    // Run every 10 minutes as a backup check
    @Scheduled(fixedRate = 600000) // 10 minutes = 600000 milliseconds  
    public void updateExpiredAuctionsBackup() {
        try {
            productService.updateExpiredAuctions();
        } catch (Exception e) {
            System.err.println("Error in backup expired auctions update: " + e.getMessage());
        }
    }
}
