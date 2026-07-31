package com.auction.infrastructure.adapter.out.persistence.repository;

import com.auction.infrastructure.adapter.out.persistence.entity.BidJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataBidRepository extends JpaRepository<BidJpaEntity, Long> {
    List<BidJpaEntity> findByAuctionIdOrderByAmountDesc(Long auctionId);
    Optional<BidJpaEntity> findByIdempotencyKey(String idempotencyKey);
}
