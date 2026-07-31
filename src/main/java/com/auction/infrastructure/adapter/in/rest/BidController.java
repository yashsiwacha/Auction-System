package com.auction.infrastructure.adapter.in.rest;

import com.auction.domain.model.Bid;
import com.auction.domain.port.in.PlaceBidUseCase;
import com.auction.infrastructure.adapter.in.rest.dto.PlaceBidRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auctions/{auctionId}/bids")
@RequiredArgsConstructor
@Tag(name = "Bids", description = "Endpoints for placing real-time bids under high concurrency")
public class BidController {

    private final PlaceBidUseCase placeBidUseCase;

    @PostMapping
    @Operation(summary = "Place a new bid on an active auction with idempotency support")
    public ResponseEntity<Bid> placeBid(
            @PathVariable Long auctionId,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyHeader,
            @Valid @RequestBody PlaceBidRequest request) {

        String key = (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank())
                ? request.getIdempotencyKey()
                : idempotencyHeader;

        Bid bid = placeBidUseCase.placeBid(auctionId, request.getBidderId(), request.getAmount(), key);
        return ResponseEntity.status(HttpStatus.CREATED).body(bid);
    }
}
