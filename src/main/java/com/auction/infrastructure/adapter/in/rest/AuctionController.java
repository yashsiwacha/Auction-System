package com.auction.infrastructure.adapter.in.rest;

import com.auction.domain.model.Auction;
import com.auction.domain.model.AuctionStatus;
import com.auction.domain.model.Bid;
import com.auction.domain.port.in.CreateAuctionUseCase;
import com.auction.domain.port.in.GetAuctionUseCase;
import com.auction.infrastructure.adapter.in.rest.dto.CreateAuctionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
@Tag(name = "Auctions", description = "Endpoints for creating and retrieving auction items")
public class AuctionController {

    private final CreateAuctionUseCase createAuctionUseCase;
    private final GetAuctionUseCase getAuctionUseCase;

    @PostMapping
    @Operation(summary = "Create a new auction")
    public ResponseEntity<Auction> createAuction(@Valid @RequestBody CreateAuctionRequest request) {
        Auction auction = createAuctionUseCase.createAuction(
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getStartingPrice(),
                request.getSellerId(),
                request.getStartTime(),
                request.getEndTime()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(auction);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get auction details by ID")
    public ResponseEntity<Auction> getAuctionById(@PathVariable Long id) {
        Auction auction = getAuctionUseCase.getAuctionById(id);
        return ResponseEntity.ok(auction);
    }

    @GetMapping
    @Operation(summary = "List auctions filtered by status")
    public ResponseEntity<List<Auction>> getAuctions(@RequestParam(required = false, defaultValue = "ACTIVE") AuctionStatus status) {
        List<Auction> auctions = getAuctionUseCase.getAuctionsByStatus(status);
        return ResponseEntity.ok(auctions);
    }

    @GetMapping("/{id}/bids")
    @Operation(summary = "Get bid history for a specific auction")
    public ResponseEntity<List<Bid>> getAuctionBids(@PathVariable Long id) {
        List<Bid> bids = getAuctionUseCase.getBidsForAuction(id);
        return ResponseEntity.ok(bids);
    }
}
