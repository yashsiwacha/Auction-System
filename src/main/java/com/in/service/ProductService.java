package com.in.service;

import com.in.entity.Product;
import com.in.entity.User;
import com.in.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    public Product createProduct(String title, String description, BigDecimal startingPrice,
                               BigDecimal reservePrice, LocalDateTime auctionStartTime,
                               LocalDateTime auctionEndTime, User seller, String imageUrl) {
        Product product = new Product();
        product.setTitle(title);
        product.setDescription(description);
        product.setStartingPrice(startingPrice);
        product.setCurrentHighestBid(startingPrice);
        product.setReservePrice(reservePrice);
        product.setAuctionStartTime(auctionStartTime);
        product.setAuctionEndTime(auctionEndTime);
        product.setSeller(seller);
        product.setImageUrl(imageUrl);
        product.setStatus(Product.ProductStatus.PENDING);
        
        return saveProduct(product);
    }
    
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }
    
    public List<Product> findProductsByStatus(Product.ProductStatus status) {
        return productRepository.findByStatus(status);
    }
    
    public List<Product> findProductsBySeller(User seller) {
        return productRepository.findBySeller(seller);
    }
    
    public List<Product> findActiveAuctions() {
        return productRepository.findActiveAuctions(LocalDateTime.now());
    }
    
    public List<Product> findEndedAuctions() {
        return productRepository.findEndedAuctions(LocalDateTime.now());
    }
    
    public List<Product> findUpcomingAuctions() {
        return productRepository.findUpcomingAuctions(LocalDateTime.now());
    }
    
    public List<Product> findApprovedProducts() {
        return productRepository.findAllApprovedProducts();
    }
    
    public Optional<Product> findById(Integer id) {
        return productRepository.findOne(id) != null ? 
               Optional.of(productRepository.findOne(id)) : Optional.empty();
    }
    
    public Product approveProduct(Integer productId) {
        Product product = productRepository.findOne(productId);
        if (product != null) {
            product.setStatus(Product.ProductStatus.APPROVED);
            return saveProduct(product);
        }
        throw new RuntimeException("Product not found");
    }
    
    public Product rejectProduct(Integer productId) {
        Product product = productRepository.findOne(productId);
        if (product != null) {
            product.setStatus(Product.ProductStatus.REJECTED);
            return saveProduct(product);
        }
        throw new RuntimeException("Product not found");
    }
    
    public Product updateProduct(Product product) {
        return saveProduct(product);
    }
    
    public Product updateCurrentHighestBid(Integer productId, BigDecimal newBidAmount) {
        Product product = productRepository.findOne(productId);
        if (product != null) {
            product.setCurrentHighestBid(newBidAmount);
            return saveProduct(product);
        }
        throw new RuntimeException("Product not found");
    }
    
    public void endAuction(Integer productId, User winner) {
        Product product = productRepository.findOne(productId);
        if (product != null) {
            product.setStatus(Product.ProductStatus.COMPLETED);
            product.setWinner(winner);
            saveProduct(product);
        }
    }
    
    public void updateExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Product> expiredProducts = productRepository.findEndedAuctions(now);
        
        for (Product product : expiredProducts) {
            if (product.getStatus() == Product.ProductStatus.APPROVED) {
                product.setStatus(Product.ProductStatus.COMPLETED);
                saveProduct(product);
            }
        }
    }
    
    public void deleteProduct(Integer productId) {
        productRepository.delete(productId);
    }
}
