package com.in.controller;

import com.in.entity.Bid;
import com.in.entity.Product;
import com.in.entity.User;
import com.in.service.AuctionService;
import com.in.service.BidService;
import com.in.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/buyer")
public class BuyerController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private BidService bidService;
    
    @Autowired
    private AuctionService auctionService;
    
    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.BUYER) {
            return "redirect:/login";
        }
        
        List<Product> activeAuctions = auctionService.getActiveAuctionsForBuyer();
        List<Bid> userBids = bidService.findBidsByBidder(user);
        
        model.addAttribute("activeAuctions", activeAuctions);
        model.addAttribute("userBids", userBids);
        model.addAttribute("user", user);
        
        // Calculate winning bids count
        long winningBidsCount = userBids.stream()
                .filter(bid -> bid.getStatus() == Bid.BidStatus.WINNING)
                .count();
        
        model.addAttribute("winningBidsCount", winningBidsCount);
        
        return "buyer/dashboard";
    }
    
    @RequestMapping(value = "/auctions", method = RequestMethod.GET)
    public String viewAuctions(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.BUYER) {
            return "redirect:/login";
        }
        
        List<Product> allProducts = productService.findApprovedProducts();
        // Ensure auctions is never null
        if (allProducts == null) {
            allProducts = new java.util.ArrayList<>();
        }
        model.addAttribute("auctions", allProducts);
        model.addAttribute("user", user);
        
        return "buyer/auctions";
    }
    
    @RequestMapping(value = "/auction/{id}", method = RequestMethod.GET)
    public String viewAuction(@PathVariable Integer id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.BUYER) {
            return "redirect:/login";
        }
        
        Optional<Product> productOpt = productService.findById(id);
        if (!productOpt.isPresent()) {
            return "redirect:/buyer/auctions";
        }
        
        Product product = productOpt.get();
        List<Bid> productBids = bidService.findBidsByProduct(product);
        BigDecimal minimumBid = bidService.getMinimumBidAmount(product);
        boolean canBid = auctionService.canUserBid(user, product);
        
        model.addAttribute("product", product);
        model.addAttribute("bids", productBids);
        model.addAttribute("minimumBid", minimumBid);
        model.addAttribute("canBid", canBid);
        model.addAttribute("user", user);
        
        return "buyer/auction-detail";
    }
    
    @RequestMapping(value = "/auction/{id}/bid", method = RequestMethod.POST)
    public String placeBid(@PathVariable Integer id,
                          @RequestParam BigDecimal bidAmount,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.BUYER) {
            return "redirect:/login";
        }
        
        try {
            Optional<Product> productOpt = productService.findById(id);
            if (!productOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Product not found");
                return "redirect:/buyer/auctions";
            }
            
            Product product = productOpt.get();
            
            if (!auctionService.canUserBid(user, product)) {
                redirectAttributes.addFlashAttribute("error", "You cannot bid on this auction");
                return "redirect:/buyer/auction/" + id;
            }
            
            bidService.placeBid(product, user, bidAmount);
            redirectAttributes.addFlashAttribute("success", "Bid placed successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/buyer/auction/" + id;
    }
    
    @RequestMapping(value = "/my-bids", method = RequestMethod.GET)
    public String viewMyBids(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.BUYER) {
            return "redirect:/login";
        }
        
        List<Bid> userBids = bidService.findBidsByBidder(user);
        
        // Categorize bids
        List<Bid> allBids = userBids;
        List<Bid> winningBids = new ArrayList<>();
        List<Bid> outbidBids = new ArrayList<>();
        List<Bid> wonBids = new ArrayList<>();
        
        for (Bid bid : userBids) {
            if (bid.getStatus() == Bid.BidStatus.WINNING) {
                winningBids.add(bid);
            } else if (bid.getStatus() == Bid.BidStatus.OUTBID) {
                outbidBids.add(bid);
            } else if (bid.getStatus() == Bid.BidStatus.WON) {
                wonBids.add(bid);
            }
        }
        
        model.addAttribute("allBids", allBids);
        model.addAttribute("winningBids", winningBids);
        model.addAttribute("outbidBids", outbidBids);
        model.addAttribute("wonBids", wonBids);
        model.addAttribute("user", user);
        
        return "buyer/my-bids";
    }
    
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String searchAuctions(@RequestParam(required = false) String query,
                                HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.BUYER) {
            return "redirect:/login";
        }
        
        List<Product> products;
        if (query != null && !query.trim().isEmpty()) {
            // Simple search by title (in a real application, you'd use a proper search engine)
            products = productService.findApprovedProducts().stream()
                .filter(p -> p.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                           p.getDescription().toLowerCase().contains(query.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
        } else {
            products = productService.findApprovedProducts();
        }
        
        model.addAttribute("searchResults", products);
        model.addAttribute("products", products);
        model.addAttribute("query", query);
        model.addAttribute("user", user);
        
        return "buyer/search";
    }
}
