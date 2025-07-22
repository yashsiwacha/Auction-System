package com.in.controller;

import com.in.entity.AuctionSlot;
import com.in.entity.Product;
import com.in.entity.User;
import com.in.service.AuctionSlotService;
import com.in.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/seller")
public class SellerController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private AuctionSlotService auctionSlotService;
    
    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String dashboard(HttpSession session, Model model) {
        System.out.println("=== SELLER DASHBOARD ACCESS ===");
        System.out.println("Session ID: " + session.getId());
        System.out.println("Session creation time: " + new java.util.Date(session.getCreationTime()));
        System.out.println("Session last accessed: " + new java.util.Date(session.getLastAccessedTime()));
        
        User user = (User) session.getAttribute("user");
        System.out.println("User from session: " + (user != null ? user.getUsername() : "NULL"));
        
        if (user == null || user.getRole() != User.UserRole.SELLER) {
            System.out.println("Redirecting to login - user: " + user + " role: " + (user != null ? user.getRole() : "NULL"));
            return "redirect:/login";
        }
        
        List<Product> userProducts = productService.findProductsBySeller(user);
        model.addAttribute("products", userProducts);
        model.addAttribute("user", user);
        
        // Calculate counts for statistics
        long pendingCount = userProducts.stream()
                .filter(p -> p.getStatus() == Product.ProductStatus.PENDING)
                .count();
        long completedCount = userProducts.stream()
                .filter(p -> p.getStatus() == Product.ProductStatus.COMPLETED)
                .count();
        
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("completedCount", completedCount);
        
        return "seller/dashboard";
    }
    
    @RequestMapping(value = "/products", method = RequestMethod.GET)
    public String viewProducts(HttpSession session, Model model) {
        System.out.println("=== SELLER PRODUCTS ACCESS ===");
        System.out.println("Session ID: " + session.getId());
        
        User user = (User) session.getAttribute("user");
        System.out.println("User from session: " + (user != null ? user.getUsername() : "NULL"));
        
        if (user == null || user.getRole() != User.UserRole.SELLER) {
            System.out.println("Redirecting to login - user: " + user + " role: " + (user != null ? user.getRole() : "NULL"));
            return "redirect:/login";
        }
        
        List<Product> userProducts = productService.findProductsBySeller(user);
        model.addAttribute("products", userProducts);
        model.addAttribute("user", user);
        
        return "seller/products";
    }
    
    @RequestMapping(value = "/product/add", method = RequestMethod.GET)
    public String addProductForm(HttpSession session, Model model) {
        System.out.println("=== ADD PRODUCT FORM ACCESS ===");
        
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.SELLER) {
            return "redirect:/login";
        }
        
        // Get available auction slots from today onwards
        List<AuctionSlot> availableSlots = auctionSlotService.getAvailableSlotsFromDate(LocalDate.now());
        
        System.out.println("Available slots count: " + availableSlots.size());
        
        // If no slots are available, auto-generate slots for the next 7 days
        if (availableSlots.isEmpty()) {
            System.out.println("No slots found, auto-generating slots for next 7 days");
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 7; i++) {
                LocalDate slotDate = today.plusDays(i);
                try {
                    auctionSlotService.generateSlotsForDate(slotDate);
                    System.out.println("Generated slots for: " + slotDate);
                } catch (Exception e) {
                    System.out.println("Failed to generate slots for " + slotDate + ": " + e.getMessage());
                }
            }
            
            // Fetch available slots again after generation
            availableSlots = auctionSlotService.getAvailableSlotsFromDate(LocalDate.now());
            System.out.println("Available slots after generation: " + availableSlots.size());
        }
        
        for (AuctionSlot slot : availableSlots) {
            System.out.println("Slot: " + slot.getSlotDate() + " " + slot.getStartTime() + "-" + slot.getEndTime() + " (" + slot.getCurrentProducts() + "/" + slot.getMaxProducts() + ")");
        }
        
        model.addAttribute("user", user);
        model.addAttribute("availableSlots", availableSlots);
        return "seller/add-product";
    }
    
    @RequestMapping(value = "/product/add", method = RequestMethod.POST)
    public String addProduct(@RequestParam String title,
                            @RequestParam String description,
                            @RequestParam BigDecimal startingPrice,
                            @RequestParam(required = false) BigDecimal reservePrice,
                            @RequestParam Integer auctionSlotId,
                            @RequestParam(required = false) String imageUrl,
                            HttpSession session,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.SELLER) {
            return "redirect:/login";
        }
        
        try {
            // Validation
            if (startingPrice.compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("error", "Starting price must be greater than zero");
                return "redirect:/seller/product/add";
            }
            
            // Check if auction slot is available
            if (!auctionSlotService.canAddProductToSlot(auctionSlotId)) {
                redirectAttributes.addFlashAttribute("error", "Selected auction slot is full or unavailable");
                return "redirect:/seller/product/add";
            }
            
            // Get the auction slot
            Optional<AuctionSlot> slotOpt = auctionSlotService.findById(auctionSlotId);
            if (!slotOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Invalid auction slot selected");
                return "redirect:/seller/product/add";
            }
            
            AuctionSlot slot = slotOpt.get();
            
            // Create auction start and end times based on slot
            LocalDateTime auctionStartTime = LocalDateTime.of(slot.getSlotDate(), slot.getStartTime());
            LocalDateTime auctionEndTime = LocalDateTime.of(slot.getSlotDate(), slot.getEndTime());
            
            // Create and save product
            Product product = productService.createProduct(
                title, description, startingPrice, reservePrice,
                auctionStartTime, auctionEndTime, user, imageUrl
            );
            
            // Associate product with slot
            product.setAuctionSlot(slot);
            productService.updateProduct(product);
            
            // Update slot product count
            auctionSlotService.addProductToSlot(auctionSlotId);
            
            redirectAttributes.addFlashAttribute("success", 
                "Product added successfully! It will be reviewed by admin before going live in the " + 
                slot.getSlotDate() + " " + slot.getTimeRange() + " slot.");
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding product: " + e.getMessage());
        }
        
        return "redirect:/seller/products";
    }
    
    @RequestMapping(value = "/product/edit/{id}", method = RequestMethod.GET)
    public String editProductForm(@PathVariable Integer id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.SELLER) {
            return "redirect:/login";
        }
        
        // Product editing has been disabled
        return "redirect:/seller/products";
    }
    
    @RequestMapping(value = "/product/edit/{id}", method = RequestMethod.POST)
    public String editProduct(@PathVariable Integer id,
                             @RequestParam String title,
                             @RequestParam String description,
                             @RequestParam BigDecimal startingPrice,
                             @RequestParam(required = false) BigDecimal reservePrice,
                             @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime auctionStartTime,
                             @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime auctionEndTime,
                             @RequestParam(required = false) String imageUrl,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.SELLER) {
            return "redirect:/login";
        }
        
        // Product editing has been disabled
        return "redirect:/seller/products";
    }
    
    @RequestMapping(value = "/product/delete/{id}", method = RequestMethod.POST)
    public String deleteProduct(@PathVariable Integer id, 
                               HttpSession session, 
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.SELLER) {
            return "redirect:/login";
        }
        
        try {
            Product product = productService.findById(id).orElse(null);
            if (product == null || !product.getSeller().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Product not found or access denied");
                return "redirect:/seller/products";
            }
            
            // Check if product can be deleted (before slot starts)
            if (!product.canBeDeleted()) {
                redirectAttributes.addFlashAttribute("error", "Cannot delete product after auction slot has started");
                return "redirect:/seller/products";
            }
            
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/seller/products";
    }
}
