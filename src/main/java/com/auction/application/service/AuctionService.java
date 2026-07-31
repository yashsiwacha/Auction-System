package com.auction.application.service;

import com.auction.domain.exception.AuctionNotFoundException;
import com.auction.domain.model.Auction;
import com.auction.domain.model.AuctionStatus;
import com.auction.domain.model.Bid;
import com.auction.domain.port.in.CreateAuctionUseCase;
import com.auction.domain.port.in.GetAuctionUseCase;
import com.auction.domain.port.out.AuctionRepositoryPort;
import com.auction.domain.port.out.BidRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service("domainAuctionService")
@RequiredArgsConstructor
public class AuctionService implements CreateAuctionUseCase, GetAuctionUseCase {

    private final AuctionRepositoryPort auctionRepository;
    private final BidRepositoryPort bidRepository;

    @Override
    @Transactional
    public Auction createAuction(String title, String description, String category, BigDecimal startingPrice, Long sellerId, Instant startTime, Instant endTime) {
        Auction auction = Auction.builder()
                .title(title)
                .description(description)
                .category(category != null ? category : "GENERAL")
                .startingPrice(startingPrice)
                .currentPrice(startingPrice)
                .sellerId(sellerId)
                .status(AuctionStatus.ACTIVE)
                .version(0L)
                .startTime(startTime != null ? startTime : Instant.now())
                .endTime(endTime)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Auction saved = auctionRepository.save(auction);
        log.info("Auction created successfully with ID: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Auction getAuctionById(Long id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new AuctionNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Auction> getAuctionsByStatus(AuctionStatus status) {
        return auctionRepository.findAllByStatus(status != null ? status : AuctionStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bid> getBidsForAuction(Long auctionId) {
        return bidRepository.findByAuctionIdOrderByAmountDesc(auctionId);
    }
}
