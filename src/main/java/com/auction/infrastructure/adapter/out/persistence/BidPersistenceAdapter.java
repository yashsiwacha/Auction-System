package com.auction.infrastructure.adapter.out.persistence;

import com.auction.domain.model.Bid;
import com.auction.domain.port.out.BidRepositoryPort;
import com.auction.infrastructure.adapter.out.persistence.entity.BidJpaEntity;
import com.auction.infrastructure.adapter.out.persistence.repository.SpringDataBidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BidPersistenceAdapter implements BidRepositoryPort {

    private final SpringDataBidRepository bidRepository;

    @Override
    public Bid save(Bid bid) {
        BidJpaEntity entity = toEntity(bid);
        BidJpaEntity saved = bidRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Bid> findByAuctionIdOrderByAmountDesc(Long auctionId) {
        return bidRepository.findByAuctionIdOrderByAmountDesc(auctionId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Bid> findByIdempotencyKey(String idempotencyKey) {
        return bidRepository.findByIdempotencyKey(idempotencyKey).map(this::toDomain);
    }

    private BidJpaEntity toEntity(Bid domain) {
        return BidJpaEntity.builder()
                .id(domain.getId())
                .auctionId(domain.getAuctionId())
                .bidderId(domain.getBidderId())
                .amount(domain.getAmount())
                .status(domain.getStatus())
                .idempotencyKey(domain.getIdempotencyKey())
                .timestamp(domain.getTimestamp())
                .build();
    }

    private Bid toDomain(BidJpaEntity entity) {
        return Bid.builder()
                .id(entity.getId())
                .auctionId(entity.getAuctionId())
                .bidderId(entity.getBidderId())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .idempotencyKey(entity.getIdempotencyKey())
                .timestamp(entity.getTimestamp())
                .build();
    }
}
