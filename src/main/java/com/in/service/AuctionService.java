package com.in.service;

import com.in.entity.Bid;
import com.in.entity.Product;
import com.in.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AuctionService {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private BidService bidService;
    
    public void processEndedAuctions() {
        List<Product> endedAuctions = productService.findEndedAuctions();
        
        for (Product product : endedAuctions) {
            if (product.getStatus() == Product.ProductStatus.APPROVED) {
                Optional<Bid> highestBid = bidService.findHighestBidForProduct(product);
                
                if (highestBid.isPresent()) {
                    Bid winningBid = highestBid.get();
                    // Check if reserve price is met
                    if (product.getReservePrice() == null || 
                        winningBid.getBidAmount().compareTo(product.getReservePrice()) >= 0) {
                        
                        // Mark as won
                        bidService.markBidAsWon(winningBid.getId());
                        productService.endAuction(product.getId(), winningBid.getBidder());
                    } else {
                        // Reserve price not met - auction fails
                        product.setStatus(Product.ProductStatus.CANCELLED);
                        productService.updateProduct(product);
                    }
                } else {
                    // No bids - auction fails
                    product.setStatus(Product.ProductStatus.CANCELLED);
                    productService.updateProduct(product);
                }
            }
        }
    }
    
    public boolean canUserBid(User user, Product product) {
        if (user == null || product == null) {
            return false;
        }
        
        // Check if user is active
        if (!user.getIsActive()) {
            return false;
        }
        
        // Only buyers can bid
        if (user.getRole() != User.UserRole.BUYER) {
            return false;
        }
        
        // Seller cannot bid on their own product
        if (product.getSeller().getId().equals(user.getId())) {
            return false;
        }
        
        // Check if auction is active
        return product.isAuctionActive();
    }
    
    public boolean canUserManageProduct(User user, Product product) {
        if (user == null || product == null) {
            return false;
        }
        
        // Admin can manage any product
        if (user.getRole() == User.UserRole.ADMIN) {
            return true;
        }
        
        // Seller can manage their own products (before approval)
        return user.getRole() == User.UserRole.SELLER && 
               product.getSeller().getId().equals(user.getId());
    }
    
    public List<Product> getProductsForUser(User user) {
        switch (user.getRole()) {
            case ADMIN:
                return productService.findAllProducts();
            case SELLER:
                return productService.findProductsBySeller(user);
            case BUYER:
                return productService.findApprovedProducts();
            default:
                return List.of();
        }
    }
    
    public List<Product> getActiveAuctionsForBuyer() {
        return productService.findActiveAuctions();
    }
    
    public List<Product> getPendingApprovalsForAdmin() {
        return productService.findProductsByStatus(Product.ProductStatus.PENDING);
    }
    
    public String getAuctionStatusMessage(Product product) {
        LocalDateTime now = LocalDateTime.now();
        
        if (product.getStatus() != Product.ProductStatus.APPROVED) {
            return "Auction " + product.getStatus().toString().toLowerCase();
        }
        
        if (now.isBefore(product.getAuctionStartTime())) {
            return "Auction starts at " + product.getAuctionStartTime();
        } else if (now.isAfter(product.getAuctionEndTime())) {
            return "Auction ended at " + product.getAuctionEndTime();
        } else {
            return "Active until " + product.getAuctionEndTime();
        }
    }
}
