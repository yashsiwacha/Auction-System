package com.auction.infrastructure.adapter.out.persistence;

import com.auction.domain.model.Auction;
import com.auction.domain.model.AuctionStatus;
import com.auction.domain.port.out.AuctionRepositoryPort;
import com.auction.infrastructure.adapter.out.persistence.entity.AuctionJpaEntity;
import com.auction.infrastructure.adapter.out.persistence.repository.SpringDataAuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuctionPersistenceAdapter implements AuctionRepositoryPort {

    private final SpringDataAuctionRepository auctionRepository;

    @Override
    public Auction save(Auction auction) {
        AuctionJpaEntity entity = toEntity(auction);
        AuctionJpaEntity saved = auctionRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Auction> findById(Long id) {
        return auctionRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Auction> findAllByStatus(AuctionStatus status) {
        return auctionRepository.findAllByStatus(status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private AuctionJpaEntity toEntity(Auction domain) {
        return AuctionJpaEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .category(domain.getCategory())
                .startingPrice(domain.getStartingPrice())
                .currentPrice(domain.getCurrentPrice())
                .sellerId(domain.getSellerId())
                .winnerId(domain.getWinnerId())
                .status(domain.getStatus())
                .version(domain.getVersion())
                .startTime(domain.getStartTime())
                .endTime(domain.getEndTime())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private Auction toDomain(AuctionJpaEntity entity) {
        return Auction.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .startingPrice(entity.getStartingPrice())
                .currentPrice(entity.getCurrentPrice())
                .sellerId(entity.getSellerId())
                .winnerId(entity.getWinnerId())
                .status(entity.getStatus())
                .version(entity.getVersion())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
