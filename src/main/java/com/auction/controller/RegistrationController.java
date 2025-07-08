package com.auction.controller;

import com.auction.dto.RegistrationRequest;
import com.auction.users.Admin;
import com.auction.users.Buyer;
import com.auction.users.Seller;
import com.auction.repository.AdminRepository;
import com.auction.repository.BuyerRepository;
import com.auction.repository.SellerRepository;
import com.auction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*") // Allow all origins for API registration (adjust for production)
@RestController
@RequestMapping("/api")
public class RegistrationController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private BuyerRepository buyerRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegistrationRequest request) {
        // Validation logic as before
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Password is required");
        }
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Role is required");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        switch (request.getRole().toUpperCase()) {
            case "ADMIN":
                Admin admin = new Admin(
                        request.getUsername(), request.getName(), encodedPassword, null, request.getEmail(),
                        0, "ADMIN", null, null, null, null
                );
                adminRepository.save(admin);
                break;
            case "SELLER":
                Seller seller = new Seller(
                        request.getUsername(), request.getName(), encodedPassword, null, request.getEmail(),
                        0, "SELLER", null, null, null, null
                );
                sellerRepository.save(seller);
                break;
            case "BUYER":
                Buyer buyer = new Buyer(
                        request.getUsername(), request.getName(), encodedPassword, null, request.getEmail(),
                        0, "BUYER", null, null, null, 0
                );
                buyerRepository.save(buyer);
                break;
            default:
                return ResponseEntity.badRequest().body("Invalid role");
        }
        return ResponseEntity.ok("Registration successful");
    }
}
