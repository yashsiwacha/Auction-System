package com.in.repository;

import com.in.entity.AuctionSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionSlotRepository extends JpaRepository<AuctionSlot, Integer> {
    
    List<AuctionSlot> findBySlotDateAndIsActiveTrue(LocalDate slotDate);
    
    List<AuctionSlot> findBySlotDateGreaterThanEqualAndIsActiveTrueOrderBySlotDateAscStartTimeAsc(LocalDate startDate);
    
    @Query("SELECT s FROM AuctionSlot s WHERE s.slotDate = :date AND s.startTime = :startTime AND s.isActive = true")
    Optional<AuctionSlot> findBySlotDateAndStartTime(@Param("date") LocalDate date, @Param("startTime") LocalTime startTime);
    
    @Query("SELECT s FROM AuctionSlot s WHERE s.currentProducts < s.maxProducts AND s.slotDate >= :fromDate AND s.isActive = true ORDER BY s.slotDate ASC, s.startTime ASC")
    List<AuctionSlot> findAvailableSlots(@Param("fromDate") LocalDate fromDate);
    
    @Query("SELECT s FROM AuctionSlot s WHERE s.slotDate = :date AND s.startTime >= :startTime AND s.endTime <= :endTime AND s.isActive = true")
    List<AuctionSlot> findSlotsInTimeRange(@Param("date") LocalDate date, @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);
}
