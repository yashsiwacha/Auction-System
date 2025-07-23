package com.in.controller;

import com.in.entity.Product;
import com.in.entity.User;
import com.in.service.AuctionService;
import com.in.service.ProductService;
import com.in.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private AuctionService auctionService;
    
    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/login";
        }
        
        List<Product> pendingProducts = auctionService.getPendingApprovalsForAdmin();
        List<Product> allProducts = productService.findAllProducts();
        List<User> allUsers = userService.findAllUsers();
        
        model.addAttribute("pendingProducts", pendingProducts);
        model.addAttribute("allProducts", allProducts);
        model.addAttribute("allUsers", allUsers);
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
    
    @RequestMapping(value = "/slots", method = RequestMethod.GET)
    public String manageSlots(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            return "redirect:/login";
        }
        
        // Redirect to the slots management controller
        return "redirect:/admin/slots";
    }
}
