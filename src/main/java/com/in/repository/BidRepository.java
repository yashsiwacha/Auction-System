package com.in.repository;

import com.in.entity.Bid;
import com.in.entity.Product;
import com.in.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Integer> {
    
    List<Bid> findByProduct(Product product);
    
    List<Bid> findByBidder(User bidder);
    
    List<Bid> findByProductOrderByBidAmountDesc(Product product);
    
    @Query("SELECT b FROM Bid b WHERE b.product = :product ORDER BY b.bidAmount DESC")
    List<Bid> findByProductOrderByBidAmountDescending(@Param("product") Product product);
    
    @Query("SELECT b FROM Bid b WHERE b.product = :product AND b.bidAmount = (SELECT MAX(b2.bidAmount) FROM Bid b2 WHERE b2.product = :product)")
    Optional<Bid> findHighestBidForProduct(@Param("product") Product product);
    
    @Query("SELECT b FROM Bid b WHERE b.bidder = :bidder ORDER BY b.bidTime DESC")
    List<Bid> findByBidderOrderByBidTimeDesc(@Param("bidder") User bidder);
    
    List<Bid> findByStatus(Bid.BidStatus status);
}
