package com.in.service;

import com.in.entity.AuctionSlot;
import com.in.repository.AuctionSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AuctionSlotService {
    
    @Autowired
    private AuctionSlotRepository auctionSlotRepository;
    
    // Business hours: 9 AM to 5 PM
    private static final LocalTime BUSINESS_START = LocalTime.of(9, 0);
    private static final LocalTime BUSINESS_END = LocalTime.of(17, 0);
    
    public List<AuctionSlot> generateSlotsForDate(LocalDate date) {
        List<AuctionSlot> slots = new ArrayList<>();
        LocalTime currentTime = BUSINESS_START;
        
        while (currentTime.isBefore(BUSINESS_END)) {
            LocalTime endTime = currentTime.plusHours(1);
            
            // Check if slot already exists
            Optional<AuctionSlot> existingSlot = auctionSlotRepository.findBySlotDateAndStartTime(date, currentTime);
            if (!existingSlot.isPresent()) {
                AuctionSlot slot = new AuctionSlot(date, currentTime, endTime);
                slots.add(auctionSlotRepository.save(slot));
            } else {
                slots.add(existingSlot.get());
            }
            
            currentTime = endTime;
        }
        
        return slots;
    }
    
    public List<AuctionSlot> getAvailableSlotsFromDate(LocalDate fromDate) {
        return auctionSlotRepository.findAvailableSlots(fromDate);
    }
    
    public List<AuctionSlot> getSlotsForDate(LocalDate date) {
        return auctionSlotRepository.findBySlotDateAndIsActiveTrue(date);
    }
    
    public Optional<AuctionSlot> findById(Integer id) {
        return auctionSlotRepository.findOne(id) != null ? 
               Optional.of(auctionSlotRepository.findOne(id)) : 
               Optional.empty();
    }
    
    public AuctionSlot save(AuctionSlot slot) {
        return auctionSlotRepository.save(slot);
    }
    
    public void delete(AuctionSlot slot) {
        auctionSlotRepository.delete(slot);
    }
    
    public boolean canAddProductToSlot(Integer slotId) {
        Optional<AuctionSlot> slot = findById(slotId);
        return slot.isPresent() && slot.get().hasAvailableSlots();
    }
    
    public void addProductToSlot(Integer slotId) {
        Optional<AuctionSlot> slot = findById(slotId);
        if (slot.isPresent() && slot.get().hasAvailableSlots()) {
            slot.get().incrementProductCount();
            save(slot.get());
        }
    }
    
    public void removeProductFromSlot(Integer slotId) {
        Optional<AuctionSlot> slot = findById(slotId);
        if (slot.isPresent()) {
            slot.get().decrementProductCount();
            save(slot.get());
        }
    }
    
    public List<AuctionSlot> getAllSlots() {
        return auctionSlotRepository.findAll();
    }
    
    public void deactivateSlot(Integer slotId) {
        Optional<AuctionSlot> slot = findById(slotId);
        if (slot.isPresent()) {
            slot.get().setIsActive(false);
            save(slot.get());
        }
    }
    
    public void activateSlot(Integer slotId) {
        Optional<AuctionSlot> slot = findById(slotId);
        if (slot.isPresent()) {
            slot.get().setIsActive(true);
            save(slot.get());
        }
    }
}
