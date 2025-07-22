package com.in.controller;

import com.in.entity.AuctionSlot;
import com.in.entity.User;
import com.in.service.AuctionSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/slots")
public class SlotManagementController {
    
    @Autowired
    private AuctionSlotService auctionSlotService;
    
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String viewSlots(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/login";
        }
        
        // Show upcoming slots from today onwards, but if it's late in the day (after 6 PM), 
        // focus on tomorrow's slots
        LocalDate fromDate = LocalDate.now();
        if (java.time.LocalTime.now().isAfter(java.time.LocalTime.of(18, 0))) {
            fromDate = LocalDate.now().plusDays(1);
        }
        
        List<AuctionSlot> slots = auctionSlotService.getAvailableSlotsFromDate(fromDate);
        model.addAttribute("slots", slots);
        model.addAttribute("user", user);
        
        return "admin/slots";
    }
    
    @RequestMapping(value = "/generate", method = RequestMethod.POST)
    public String generateSlots(@RequestParam String date, 
                               HttpSession session, 
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/login";
        }
        
        try {
            LocalDate slotDate = LocalDate.parse(date);
            auctionSlotService.generateSlotsForDate(slotDate);
            redirectAttributes.addFlashAttribute("success", "Slots generated successfully for " + date);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error generating slots: " + e.getMessage());
        }
        
        return "redirect:/admin/slots";
    }
    
    @RequestMapping(value = "/deactivate/{id}", method = RequestMethod.POST)
    public String deactivateSlot(@PathVariable Integer id, 
                                HttpSession session, 
                                RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/login";
        }
        
        try {
            auctionSlotService.deactivateSlot(id);
            redirectAttributes.addFlashAttribute("success", "Slot deactivated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deactivating slot: " + e.getMessage());
        }
        
        return "redirect:/admin/slots";
    }
    
    @RequestMapping(value = "/activate/{id}", method = RequestMethod.POST)
    public String activateSlot(@PathVariable Integer id, 
                              HttpSession session, 
                              RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/login";
        }
        
        try {
            auctionSlotService.activateSlot(id);
            redirectAttributes.addFlashAttribute("success", "Slot activated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error activating slot: " + e.getMessage());
        }
        
        return "redirect:/admin/slots";
    }
}
