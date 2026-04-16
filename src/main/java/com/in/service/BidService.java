package com.in.service;

import com.in.entity.Bid;
import com.in.entity.Product;
import com.in.entity.User;
import com.in.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Autowired
    private UserService userService;

    @Autowired(required = false)
    private RedisBidCacheService redisBidCacheService;

    @Autowired(required = false)
    private KafkaBidEventPublisher kafkaBidEventPublisher;

    @Value("${auction.bid.minimum-increment:0.01}")
    private String minimumIncrementValue;

    @Value("${auction.bid.max-scale:2}")
    private Integer maxScale;

    @Value("${auction.bid.rate-limit.per-second:20}")
    private Integer bidRateLimitPerSecond;
    
    public Bid saveBid(Bid bid) {
        return bidRepository.save(bid);
    }

    public Bid placeBid(Product product, User bidder, BigDecimal bidAmount) {
        if (product == null || product.getId() == null) {
            throw new RuntimeException("Product not found");
        }
        return placeBid(product.getId(), bidder, bidAmount);
    }

    public Bid placeBid(Integer productId, Integer bidderId, BigDecimal bidAmount) {
        User bidder = userService.findById(bidderId)
            .orElseThrow(() -> new RuntimeException("Bidder not found"));
        return placeBid(productId, bidder, bidAmount);
    }

    public Bid placeBid(Integer productId, User bidder, BigDecimal bidAmount) {
        if (bidder == null || bidder.getId() == null) {
            throw new RuntimeException("Bidder not found");
        }
        if (bidAmount == null || bidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Bid amount must be greater than zero");
        }

        if (redisBidCacheService != null && !redisBidCacheService.tryAcquireBidPermit(productId, bidder.getId(), safeRateLimitPerSecond())) {
            throw new RuntimeException("Too many bid requests. Please slow down.");
        }

        Product lockedProduct = productService.findByIdForUpdate(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        validateBidEligibility(lockedProduct, bidder, bidAmount);

        Bid currentWinningBid = findCurrentWinningBid(lockedProduct);
        BigDecimal currentHighest = resolveCurrentHighest(lockedProduct, currentWinningBid);
        BigDecimal minimumAllowedBid = currentHighest.add(minimumIncrement());

        if (bidAmount.compareTo(minimumAllowedBid) < 0) {
            throw new RuntimeException("Bid amount must be at least " + minimumAllowedBid);
        }

        bidRepository.updateStatusForProductIfCurrent(
            lockedProduct.getId(),
            Bid.BidStatus.WINNING,
            Bid.BidStatus.OUTBID
        );

        // Backward compatibility: older rows might still be ACTIVE from legacy logic.
        bidRepository.updateStatusForProductIfCurrent(
            lockedProduct.getId(),
            Bid.BidStatus.ACTIVE,
            Bid.BidStatus.OUTBID
        );

        Bid newBid = new Bid();
        newBid.setProduct(lockedProduct);
        newBid.setBidder(bidder);
        newBid.setBidAmount(bidAmount);
        newBid.setStatus(Bid.BidStatus.WINNING);
        newBid.setBidTime(LocalDateTime.now());

        Bid savedBid = saveBid(newBid);

        lockedProduct.setCurrentHighestBid(bidAmount);
        productService.saveProduct(lockedProduct);

        long totalBidCount = bidRepository.countByProduct(lockedProduct);

        if (redisBidCacheService != null) {
            redisBidCacheService.cacheBidUpdate(lockedProduct.getId(), bidAmount, totalBidCount, lockedProduct.getVersion());
        }

        if (kafkaBidEventPublisher != null) {
            kafkaBidEventPublisher.publishBidPlaced(savedBid, currentHighest, totalBidCount);
        }

        return savedBid;
    }

    private void validateBidEligibility(Product product, User bidder, BigDecimal bidAmount) {
        if (!product.isAuctionActive()) {
            throw new RuntimeException("Auction is not active");
        }

        if (product.getSeller() != null && product.getSeller().getId().equals(bidder.getId())) {
            throw new RuntimeException("Seller cannot bid on their own product");
        }

        if (bidder.getRole() != User.UserRole.BUYER) {
            throw new RuntimeException("Only buyers can place bids");
        }

        if (bidder.getIsActive() == null || !bidder.getIsActive()) {
            throw new RuntimeException("Inactive users cannot place bids");
        }

        int allowedScale = safeMaxScale();
        if (bidAmount.scale() > allowedScale) {
            throw new RuntimeException("Bid amount can have at most " + allowedScale + " decimal places");
        }
    }

    private BigDecimal resolveCurrentHighest(Product product, Bid currentWinningBid) {
        if (currentWinningBid != null && currentWinningBid.getBidAmount() != null) {
            return currentWinningBid.getBidAmount();
        }
        if (product.getCurrentHighestBid() != null) {
            return product.getCurrentHighestBid();
        }
        return product.getStartingPrice() != null ? product.getStartingPrice() : BigDecimal.ZERO;
    }
    
    public List<Bid> findBidsByProduct(Product product) {
        return bidRepository.findByProductOrderByBidAmountDescending(product);
    }
    
    public List<Bid> findBidsByBidder(User bidder) {
        return bidRepository.findByBidderOrderByBidTimeDesc(bidder);
    }
    
    public Optional<Bid> findHighestBidForProduct(Product product) {
        Bid highestBid = findCurrentWinningBid(product);
        return highestBid != null ? Optional.of(highestBid) : Optional.empty();
    }
    
    public List<Bid> findAllBids() {
        return bidRepository.findAll();
    }
    
    public Optional<Bid> findById(Integer id) {
        Bid bid = bidRepository.findById(id).orElse(null);
        return bid != null ? Optional.of(bid) : Optional.empty();
    }
    
    public void cancelBid(Integer bidId) {
        Bid bid = bidRepository.findById(bidId).orElse(null);
        if (bid != null) {
            bid.setStatus(Bid.BidStatus.CANCELLED);
            saveBid(bid);
        }
    }
    
    public void markBidAsWon(Integer bidId) {
        Bid bid = bidRepository.findById(bidId).orElse(null);
        if (bid != null) {
            bid.setStatus(Bid.BidStatus.WON);
            saveBid(bid);
        }
    }
    
    public BigDecimal getMinimumBidAmount(Product product) {
        BigDecimal highest = product.getCurrentHighestBid() != null
            ? product.getCurrentHighestBid()
            : product.getStartingPrice();
        return highest.add(minimumIncrement());
    }
    
    public long getBidCountForProduct(Product product) {
        return bidRepository.countByProduct(product);
    }

    private Bid findCurrentWinningBid(Product product) {
        List<Bid> winning = bidRepository.findWinningBidCandidates(product, Bid.BidStatus.WINNING, PageRequest.of(0, 1));
        if (!winning.isEmpty()) {
            return winning.get(0);
        }

        List<Bid> candidates = bidRepository.findHighestBidCandidates(product, PageRequest.of(0, 5));
        for (Bid candidate : candidates) {
            if (candidate.getStatus() == Bid.BidStatus.WINNING ||
                candidate.getStatus() == Bid.BidStatus.ACTIVE ||
                candidate.getStatus() == Bid.BidStatus.WON) {
                return candidate;
            }
        }

        return null;
    }

    private BigDecimal minimumIncrement() {
        try {
            BigDecimal increment = new BigDecimal(minimumIncrementValue);
            if (increment.compareTo(BigDecimal.ZERO) <= 0) {
                return new BigDecimal("0.01");
            }
            return increment.setScale(safeMaxScale(), RoundingMode.HALF_UP);
        } catch (Exception ignored) {
            return new BigDecimal("0.01");
        }
    }

    private int safeMaxScale() {
        return (maxScale == null || maxScale < 0) ? 2 : maxScale;
    }

    private int safeRateLimitPerSecond() {
        return (bidRateLimitPerSecond == null || bidRateLimitPerSecond <= 0) ? 20 : bidRateLimitPerSecond;
    }
}
