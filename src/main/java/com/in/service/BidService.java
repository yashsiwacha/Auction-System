package com.in.service;

import com.in.entity.Bid;
import com.in.entity.Product;
import com.in.entity.User;
import com.in.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BidService {
    
    @Autowired
    private BidRepository bidRepository;
    
    @Autowired
    private ProductService productService;
    
    public Bid saveBid(Bid bid) {
        return bidRepository.save(bid);
    }
    
    public Bid placeBid(Product product, User bidder, BigDecimal bidAmount) {
        // Validate bid
        if (!product.isAuctionActive()) {
            throw new RuntimeException("Auction is not active");
        }
        
        if (bidAmount.compareTo(product.getCurrentHighestBid()) <= 0) {
            throw new RuntimeException("Bid amount must be higher than current highest bid");
        }
        
        if (product.getSeller().getId().equals(bidder.getId())) {
            throw new RuntimeException("Seller cannot bid on their own product");
        }
        
        // Update previous highest bid status
        Optional<Bid> currentHighestBid = findHighestBidForProduct(product);
        if (currentHighestBid.isPresent()) {
            Bid prevBid = currentHighestBid.get();
            prevBid.setStatus(Bid.BidStatus.OUTBID);
            saveBid(prevBid);
        }
        
        // Create new bid
        Bid bid = new Bid();
        bid.setProduct(product);
        bid.setBidder(bidder);
        bid.setBidAmount(bidAmount);
        bid.setStatus(Bid.BidStatus.WINNING);
        bid.setBidTime(LocalDateTime.now());
        
        // Update product's current highest bid
        productService.updateCurrentHighestBid(product.getId(), bidAmount);
        
        return saveBid(bid);
    }
    
    public List<Bid> findBidsByProduct(Product product) {
        return bidRepository.findByProductOrderByBidAmountDescending(product);
    }
    
    public List<Bid> findBidsByBidder(User bidder) {
        return bidRepository.findByBidderOrderByBidTimeDesc(bidder);
    }
    
    public Optional<Bid> findHighestBidForProduct(Product product) {
        return bidRepository.findHighestBidForProduct(product);
    }
    
    public List<Bid> findAllBids() {
        return bidRepository.findAll();
    }
    
    public Optional<Bid> findById(Integer id) {
        return bidRepository.findOne(id) != null ? 
               Optional.of(bidRepository.findOne(id)) : Optional.empty();
    }
    
    public void cancelBid(Integer bidId) {
        Bid bid = bidRepository.findOne(bidId);
        if (bid != null) {
            bid.setStatus(Bid.BidStatus.CANCELLED);
            saveBid(bid);
        }
    }
    
    public void markBidAsWon(Integer bidId) {
        Bid bid = bidRepository.findOne(bidId);
        if (bid != null) {
            bid.setStatus(Bid.BidStatus.WON);
            saveBid(bid);
        }
    }
    
    public BigDecimal getMinimumBidAmount(Product product) {
        return product.getCurrentHighestBid().add(BigDecimal.ONE);
    }
    
    public long getBidCountForProduct(Product product) {
        return findBidsByProduct(product).size();
    }
}
