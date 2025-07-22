package com.in.controller;

import com.in.entity.User;
import com.in.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home() {
        return "redirect:/login";
    }
    
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginPage() {
        return "login";
    }
    
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestParam String username, 
                       @RequestParam String password, 
                       HttpSession session, 
                       RedirectAttributes redirectAttributes) {
        
        Optional<User> userOpt = userService.findByUsername(username);
        
        if (userOpt.isPresent() && userService.validateUser(username, password)) {
            User user = userOpt.get();
            session.setAttribute("user", user);
            
            // Redirect based on role
            switch (user.getRole()) {
                case ADMIN:
                    return "redirect:/admin/dashboard";
                case SELLER:
                    return "redirect:/seller/dashboard";
                case BUYER:
                    return "redirect:/buyer/dashboard";
                default:
                    return "redirect:/login";
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            return "redirect:/login";
        }
    }
    
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String registerPage() {
        return "register";
    }
    
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String email,
                          @RequestParam String fullName,
                          @RequestParam String phoneNumber,
                          @RequestParam String role,
                          RedirectAttributes redirectAttributes) {
        
        try {
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            userService.createUser(username, password, email, fullName, phoneNumber, userRole);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
    
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
    
    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Refresh user data
        Optional<User> updatedUser = userService.findById(user.getId());
        if (updatedUser.isPresent()) {
            model.addAttribute("user", updatedUser.get());
        }
        
        return "profile";
    }
    
    @RequestMapping(value = "/profile/update", method = RequestMethod.POST)
    public String updateProfile(@RequestParam String fullName,
                               @RequestParam String email,
                               @RequestParam String phoneNumber,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        try {
            Optional<User> userOpt = userService.findById(user.getId());
            if (userOpt.isPresent()) {
                User currentUser = userOpt.get();
                currentUser.setFullName(fullName);
                currentUser.setEmail(email);
                currentUser.setPhoneNumber(phoneNumber);
                
                userService.updateUser(currentUser);
                session.setAttribute("user", currentUser);
                
                redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/profile";
    }
    
    @RequestMapping(value = "/profile/change-password", method = RequestMethod.POST)
    public String changePassword(@RequestParam String oldPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "New passwords do not match!");
                return "redirect:/profile";
            }
            
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters long!");
                return "redirect:/profile";
            }
            
            if (userService.changePassword(user.getId(), oldPassword, newPassword)) {
                redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Current password is incorrect!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error changing password: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
}
