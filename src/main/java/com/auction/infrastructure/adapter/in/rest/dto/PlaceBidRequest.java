package com.auction.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlaceBidRequest {

    @NotNull(message = "Bidder ID is required")
    private Long bidderId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Bid amount must be greater than zero")
    private BigDecimal amount;

    private String idempotencyKey;
}
