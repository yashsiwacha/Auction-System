package com.auction.infrastructure.adapter.out.persistence.repository;

import com.auction.domain.model.AuctionStatus;
import com.auction.infrastructure.adapter.out.persistence.entity.AuctionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataAuctionRepository extends JpaRepository<AuctionJpaEntity, Long> {
    List<AuctionJpaEntity> findAllByStatus(AuctionStatus status);
}
