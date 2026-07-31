package com.auction.application.service;

import com.auction.domain.exception.AuctionNotFoundException;
import com.auction.domain.exception.BidRejectedException;
import com.auction.domain.model.Auction;
import com.auction.domain.model.Bid;
import com.auction.domain.model.BidStatus;
import com.auction.domain.port.in.PlaceBidUseCase;
import com.auction.domain.port.out.AuctionRepositoryPort;
import com.auction.domain.port.out.BidRepositoryPort;
import com.auction.domain.port.out.EventPublisherPort;
import com.auction.domain.port.out.LockPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("domainBidService")
@RequiredArgsConstructor
public class BidService implements PlaceBidUseCase {

    private final AuctionRepositoryPort auctionRepository;
    private final BidRepositoryPort bidRepository;
    private final EventPublisherPort eventPublisher;
    private final LockPort lockPort;

    @Override
    @Transactional
    public Bid placeBid(Long auctionId, Long bidderId, BigDecimal amount, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Bid> existingBid = bidRepository.findByIdempotencyKey(idempotencyKey);
            if (existingBid.isPresent()) {
                log.info("Idempotent bid request detected for key: {}", idempotencyKey);
                return existingBid.get();
            }
        }

        String lockKey = "auction:lock:" + auctionId;
        return lockPort.executeWithLock(lockKey, 5, 10, TimeUnit.SECONDS, () -> {
            Auction auction = auctionRepository.findById(auctionId)
                    .orElseThrow(() -> new AuctionNotFoundException(auctionId));

            if (!auction.isBiddable()) {
                throw new BidRejectedException("Auction is not active or has ended.");
            }

            if (auction.getSellerId().equals(bidderId)) {
                throw new BidRejectedException("Sellers cannot bid on their own auctions.");
            }

            if (amount.compareTo(auction.getCurrentPrice()) <= 0) {
                throw new BidRejectedException("Bid amount must be strictly greater than current price: " + auction.getCurrentPrice());
            }

            // Update Auction state
            auction.setCurrentPrice(amount);
            auction.setWinnerId(bidderId);
            auction.setUpdatedAt(Instant.now());
            auctionRepository.save(auction);

            // Create and persist Bid
            Bid bid = Bid.builder()
                    .auctionId(auctionId)
                    .bidderId(bidderId)
                    .amount(amount)
                    .status(BidStatus.ACCEPTED)
                    .idempotencyKey(idempotencyKey)
                    .timestamp(Instant.now())
                    .build();

            Bid savedBid = bidRepository.save(bid);
            log.info("Bid successfully placed: auctionId={}, bidderId={}, amount={}", auctionId, bidderId, amount);

            // Publish Event asynchronously
            try {
                eventPublisher.publishBidPlacedEvent(savedBid);
            } catch (Exception e) {
                log.warn("Failed to publish bid event to Kafka: {}", e.getMessage());
            }

            return savedBid;
        });
    }
}
