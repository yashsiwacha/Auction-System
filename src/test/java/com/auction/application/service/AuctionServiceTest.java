package com.auction.application.service;

import com.auction.domain.exception.AuctionNotFoundException;
import com.auction.domain.model.Auction;
import com.auction.domain.model.AuctionStatus;
import com.auction.domain.port.out.AuctionRepositoryPort;
import com.auction.domain.port.out.BidRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    private AuctionRepositoryPort auctionRepository;

    @Mock
    private BidRepositoryPort bidRepository;

    @InjectMocks
    private AuctionService auctionService;

    @Test
    @DisplayName("Should create auction with default status ACTIVE")
    void createAuction_Success() {
        when(auctionRepository.save(any(Auction.class))).thenAnswer(i -> {
            Auction a = i.getArgument(0);
            a.setId(1L);
            return a;
        });

        Auction created = auctionService.createAuction(
                "Test Item",
                "Description",
                "ELECTRONICS",
                new BigDecimal("50.00"),
                5L,
                Instant.now(),
                Instant.now().plus(2, ChronoUnit.DAYS)
        );

        assertNotNull(created);
        assertEquals(1L, created.getId());
        assertEquals(AuctionStatus.ACTIVE, created.getStatus());
        assertEquals(new BigDecimal("50.00"), created.getCurrentPrice());
    }

    @Test
    @DisplayName("Should throw exception when auction ID does not exist")
    void getAuctionById_NotFound_ThrowsException() {
        when(auctionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AuctionNotFoundException.class, () -> auctionService.getAuctionById(999L));
    }
}
