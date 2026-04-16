package com.in.controller;

import com.in.entity.Product;
import com.in.entity.User;
import com.in.service.AuctionService;
import com.in.service.AuctionStressTestService;
import com.in.service.ProductService;
import com.in.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private AuctionService auctionService;

    @Autowired
    private AuctionStressTestService auctionStressTestService;
    
    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/login";
        }
        
        List<Product> pendingProducts = auctionService.getPendingApprovalsForAdmin();
        List<Product> allProducts = productService.findAllProducts();
        List<User> allUsers = userService.findAllUsers();
        List<Product> activeAuctions = auctionService.getActiveAuctionsForBuyer();
        
        model.addAttribute("pendingProducts", pendingProducts);
        model.addAttribute("allProducts", allProducts);
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("activeAuctions", activeAuctions);
        model.addAttribute("recentStressJobs", auctionStressTestService.recentJobs());
        model.addAttribute("user", user);
        
        return "admin/dashboard";
    }
    
    @RequestMapping(value = "/products", method = RequestMethod.GET)
    public String manageProducts(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/login";
        }
        
        List<Product> allProducts = productService.findAllProducts();
        model.addAttribute("products", allProducts);
        model.addAttribute("user", user);
        
        return "admin/products";
    }
    
    @RequestMapping(value = "/product/approve/{id}", method = RequestMethod.POST)
    public String approveProduct(@PathVariable Integer id, 
                                HttpSession session, 
                                RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/login";
        }
        
        try {
            Product product = productService.approveProduct(id);
            redirectAttributes.addFlashAttribute("success", 
                "Product '" + product.getTitle() + "' approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to approve product: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    @RequestMapping(value = "/product/reject/{id}", method = RequestMethod.POST)
    public String rejectProduct(@PathVariable Integer id, 
                               HttpSession session, 
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/login";
        }
        
        try {
            Product product = productService.rejectProduct(id);
            redirectAttributes.addFlashAttribute("success", 
                "Product '" + product.getTitle() + "' rejected successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to reject product: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    @RequestMapping(value = "/product/delete/{id}", method = RequestMethod.POST)
    public String deleteProduct(@PathVariable Integer id, 
                               HttpSession session, 
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/login";
        }
        
        try {
            Product product = productService.findById(id).orElse(null);
            if (product == null) {
                redirectAttributes.addFlashAttribute("error", "Product not found");
                return "redirect:/admin/products";
            }
            
            // Check if product can be deleted (before slot starts)
            if (!product.canBeDeleted()) {
                redirectAttributes.addFlashAttribute("error", "Cannot delete product after auction slot has started");
                return "redirect:/admin/products";
            }
            
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String manageUsers(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/login";
        }
        
        List<User> allUsers = userService.findAllUsers();
        model.addAttribute("users", allUsers);
        model.addAttribute("user", user);
        
        return "admin/users";
    }
    
    @RequestMapping(value = "/user/activate/{id}", method = RequestMethod.POST)
    public String activateUser(@PathVariable Integer id, 
                              HttpSession session, 
                              RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/login";
        }
        
        try {
            userService.activateUser(id);
            redirectAttributes.addFlashAttribute("success", "User activated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    @RequestMapping(value = "/user/deactivate/{id}", method = RequestMethod.POST)
    public String deactivateUser(@PathVariable Integer id, 
                                HttpSession session, 
                                RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/login";
        }
        
        try {
            userService.deactivateUser(id);
            redirectAttributes.addFlashAttribute("success", "User deactivated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    @RequestMapping(value = "/slots-redirect", method = RequestMethod.GET)
    public String manageSlots(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/login";
        }
        
        // Redirect to the slots management controller
        return "redirect:/admin/slots";
    }

    @RequestMapping(value = "/stress-test", method = RequestMethod.POST)
    public String runStressTest(@RequestParam Integer auctionId,
                                @RequestParam(required = false) Integer totalRequests,
                                @RequestParam(required = false) Integer concurrency,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/login";
        }

        try {
            String jobId = auctionStressTestService.startStressTest(auctionId, totalRequests, concurrency);
            redirectAttributes.addFlashAttribute("success", "Stress test started. Job ID: " + jobId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unable to start stress test: " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    @RequestMapping(value = "/stress-test/{jobId}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getStressTestJob(@PathVariable String jobId,
                                                HttpSession session) {
        User user = (User) session.getAttribute("user");
        Map<String, Object> response = new HashMap<>();

        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            response.put("error", "Unauthorized");
            return response;
        }

        return auctionStressTestService.getJob(jobId)
            .<Map<String, Object>>map(job -> {
                Map<String, Object> body = new HashMap<>();
                body.put("jobId", job.getJobId());
                body.put("auctionId", job.getAuctionId());
                body.put("status", job.getStatus());
                body.put("message", job.getMessage());
                body.put("totalRequests", job.getTotalRequests());
                body.put("concurrency", job.getConcurrency());
                body.put("successCount", job.getSuccessCount());
                body.put("failureCount", job.getFailureCount());
                body.put("startedAt", job.getStartedAt());
                body.put("completedAt", job.getCompletedAt());
                return body;
            })
            .orElseGet(() -> {
                Map<String, Object> body = new HashMap<>();
                body.put("error", "Job not found");
                return body;
            });
    }
}

