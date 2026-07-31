package com.auction.application.service;

import com.auction.domain.exception.BidRejectedException;
import com.auction.domain.model.Auction;
import com.auction.domain.model.AuctionStatus;
import com.auction.domain.model.Bid;
import com.auction.domain.model.BidStatus;
import com.auction.domain.port.out.AuctionRepositoryPort;
import com.auction.domain.port.out.BidRepositoryPort;
import com.auction.domain.port.out.EventPublisherPort;
import com.auction.domain.port.out.LockPort;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock
    private AuctionRepositoryPort auctionRepository;

    @Mock
    private BidRepositoryPort bidRepository;

    @Mock
    private EventPublisherPort eventPublisher;

    @Mock
    private LockPort lockPort;

    @InjectMocks
    private BidService bidService;

    private Auction activeAuction;

    @BeforeEach
    void setUp() {
        activeAuction = Auction.builder()
                .id(1L)
                .title("Vintage Watch")
                .startingPrice(new BigDecimal("100.00"))
                .currentPrice(new BigDecimal("100.00"))
                .sellerId(10L)
                .status(AuctionStatus.ACTIVE)
                .startTime(Instant.now().minus(1, ChronoUnit.HOURS))
                .endTime(Instant.now().plus(1, ChronoUnit.HOURS))
                .version(1L)
                .build();

        lenient().when(lockPort.executeWithLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(4);
                    return supplier.get();
                });
    }

    @Test
    @DisplayName("Should successfully place a valid bid and publish event")
    void placeBid_Success() {
        Long auctionId = 1L;
        Long bidderId = 20L;
        BigDecimal newBidAmount = new BigDecimal("150.00");
        String idempotencyKey = "key-123";

        when(bidRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(activeAuction));
        when(bidRepository.save(any(Bid.class))).thenAnswer(i -> {
            Bid b = i.getArgument(0);
            b.setId(100L);
            return b;
        });

        Bid result = bidService.placeBid(auctionId, bidderId, newBidAmount, idempotencyKey);

        assertNotNull(result);
        assertEquals(newBigDecimal("150.00"), activeAuction.getCurrentPrice());
        assertEquals(bidderId, activeAuction.getWinnerId());
        assertEquals(BidStatus.ACCEPTED, result.getStatus());
        verify(eventPublisher, times(1)).publishBidPlacedEvent(any(Bid.class));
    }

    @Test
    @DisplayName("Should return existing bid when idempotency key matches")
    void placeBid_Idempotent() {
        String idempotencyKey = "existing-key";
        Bid existingBid = Bid.builder().id(99L).idempotencyKey(idempotencyKey).amount(new BigDecimal("200.00")).build();

        when(bidRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existingBid));

        Bid result = bidService.placeBid(1L, 20L, new BigDecimal("200.00"), idempotencyKey);

        assertEquals(99L, result.getId());
        verifyNoInteractions(auctionRepository);
    }

    @Test
    @DisplayName("Should reject bid if amount is lower than or equal to current price")
    void placeBid_LowerAmount_ThrowsException() {
        when(bidRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(activeAuction));

        assertThrows(BidRejectedException.class, () ->
                bidService.placeBid(1L, 20L, new BigDecimal("90.00"), "key-456")
        );
    }

    @Test
    @DisplayName("Should reject bid if seller attempts to bid on their own auction")
    void placeBid_SellerSelfBid_ThrowsException() {
        when(bidRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(activeAuction));

        assertThrows(BidRejectedException.class, () ->
                bidService.placeBid(1L, 10L, new BigDecimal("200.00"), "key-789")
        );
    }

    private BigDecimal newBigDecimal(String val) {
        return new BigDecimal(val);
    }
}
