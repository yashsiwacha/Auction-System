package com.in.controller;

import com.in.dto.BidResponse;
import com.in.dto.HighestBidResponse;
import com.in.dto.PlaceBidRequest;
import com.in.entity.Bid;
import com.in.entity.Product;
import com.in.entity.User;
import com.in.service.BidService;
import com.in.service.ProductService;
import com.in.service.RedisBidCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auctions")
public class AuctionApiController {

    @Autowired
    private ProductService productService;

    @Autowired
    private BidService bidService;

    @Autowired(required = false)
    private RedisBidCacheService redisBidCacheService;

    @RequestMapping(value = "/{auctionId}/highest-bid", method = RequestMethod.GET)
    public ResponseEntity<?> getCurrentHighestBid(@PathVariable Integer auctionId) {
        Optional<Product> productOpt = productService.findById(auctionId);
        if (!productOpt.isPresent()) {
            return new ResponseEntity<Object>(errorBody("Auction not found"), HttpStatus.NOT_FOUND);
        }

        Product product = productOpt.get();
        Optional<Bid> highestBidOpt = bidService.findHighestBidForProduct(product);

        HighestBidResponse response = new HighestBidResponse();
        response.setProductId(product.getId());
        response.setProductVersion(product.getVersion());

        long totalBidCount = bidService.getBidCountForProduct(product);
        if (redisBidCacheService != null) {
            Optional<Long> cachedCount = redisBidCacheService.getBidCount(product.getId());
            if (cachedCount.isPresent()) {
                totalBidCount = cachedCount.get();
            }
        }
        response.setTotalBidCount((int) totalBidCount);

        BigDecimal currentHighest = product.getCurrentHighestBid() != null
            ? product.getCurrentHighestBid()
            : product.getStartingPrice();

        if (redisBidCacheService != null) {
            Optional<BigDecimal> cachedHighest = redisBidCacheService.getCurrentHighestBid(product.getId());
            if (cachedHighest.isPresent() && cachedHighest.get().compareTo(currentHighest) > 0) {
                currentHighest = cachedHighest.get();
            }
        }

        if (highestBidOpt.isPresent()) {
            Bid highestBid = highestBidOpt.get();
            currentHighest = highestBid.getBidAmount();
            response.setHighestBidId(highestBid.getId());
            response.setHighestBidderId(highestBid.getBidder().getId());
            response.setHighestBidderName(highestBid.getBidder().getFullName());
            response.setHighestBidTime(highestBid.getBidTime());
        }

        response.setCurrentHighestBid(currentHighest);
        response.setMinimumNextBid(currentHighest.add(BigDecimal.ONE));

        return new ResponseEntity<Object>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/{auctionId}/bids", method = RequestMethod.POST)
    public ResponseEntity<?> placeBid(@PathVariable Integer auctionId,
                                      @RequestBody PlaceBidRequest request,
                                      HttpSession session) {
        if (request == null || request.getBidAmount() == null) {
            return new ResponseEntity<Object>(errorBody("Bid amount is required"), HttpStatus.BAD_REQUEST);
        }

        try {
            User sessionUser = (User) session.getAttribute("user");
            Bid savedBid;

            if (sessionUser != null && sessionUser.getRole() == User.UserRole.BUYER) {
                if (request.getBidderId() != null && !request.getBidderId().equals(sessionUser.getId())) {
                    return new ResponseEntity<Object>(
                        errorBody("You can only place bids for your own account"),
                        HttpStatus.FORBIDDEN
                    );
                }
                savedBid = bidService.placeBid(auctionId, sessionUser, request.getBidAmount());
            } else {
                return new ResponseEntity<Object>(
                    errorBody("Login as buyer to place bids"),
                    HttpStatus.UNAUTHORIZED
                );
            }

            return new ResponseEntity<Object>(toBidResponse(savedBid), HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            HttpStatus status = mapStatus(ex.getMessage());
            return new ResponseEntity<Object>(errorBody(ex.getMessage()), status);
        }
    }

    private BidResponse toBidResponse(Bid bid) {
        BidResponse response = new BidResponse();
        response.setBidId(bid.getId());
        response.setProductId(bid.getProduct().getId());
        response.setBidderId(bid.getBidder().getId());
        response.setBidderName(bid.getBidder().getFullName());
        response.setBidAmount(bid.getBidAmount());
        response.setStatus(bid.getStatus().name());
        response.setCurrentHighestBid(bid.getProduct().getCurrentHighestBid());
        response.setProductVersion(bid.getProduct().getVersion());
        response.setBidTime(bid.getBidTime());
        return response;
    }

    private HttpStatus mapStatus(String message) {
        if (message == null) {
            return HttpStatus.BAD_REQUEST;
        }

        String normalized = message.toLowerCase();
        if (normalized.contains("not found")) {
            return HttpStatus.NOT_FOUND;
        }
        if (normalized.contains("higher than current") || normalized.contains("not active")) {
            return HttpStatus.CONFLICT;
        }
        if (normalized.contains("only buyers") || normalized.contains("cannot") || normalized.contains("inactive")) {
            return HttpStatus.FORBIDDEN;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", message);
        body.put("timestamp", System.currentTimeMillis());
        return body;
    }
}

