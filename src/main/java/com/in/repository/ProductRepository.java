package com.in.repository;

import com.in.entity.Product;
import com.in.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    
    List<Product> findByStatus(Product.ProductStatus status);
    
    List<Product> findBySeller(User seller);
    
    List<Product> findBySellerAndStatus(User seller, Product.ProductStatus status);
    
    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' AND p.auctionStartTime <= :now AND p.auctionEndTime > :now")
    List<Product> findActiveAuctions(@Param("now") LocalDateTime now);
    
    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' AND p.auctionEndTime <= :now")
    List<Product> findEndedAuctions(@Param("now") LocalDateTime now);
    
    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' AND p.auctionStartTime > :now")
    List<Product> findUpcomingAuctions(@Param("now") LocalDateTime now);
    
    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' ORDER BY p.auctionEndTime ASC")
    List<Product> findAllApprovedProducts();
}
