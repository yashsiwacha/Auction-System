package com.in.config;

import com.in.entity.User;
import com.in.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component  // Re-enabled to create default users
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Migrate existing users from plain text to hashed passwords
        migrateExistingUsers();
        
        // Create default admin user if not exists
        if (!userService.findByUsername("admin").isPresent()) {
            userService.createUser(
                "admin", 
                "admin123", 
                "admin@auction.com", 
                "System Administrator", 
                "+1234567890", 
                User.UserRole.ADMIN
            );
            System.out.println("Default admin user created: admin/admin123");
        }
        
        // Create sample users for testing
        if (!userService.findByUsername("seller1").isPresent()) {
            userService.createUser(
                "seller1", 
                "seller123", 
                "seller1@auction.com", 
                "John Seller", 
                "+1234567891", 
                User.UserRole.SELLER
            );
            System.out.println("Sample seller user created: seller1/seller123");
        }
        
        if (!userService.findByUsername("buyer1").isPresent()) {
            userService.createUser(
                "buyer1", 
                "buyer123", 
                "buyer1@auction.com", 
                "Jane Buyer", 
                "+1234567892", 
                User.UserRole.BUYER
            );
            System.out.println("Sample buyer user created: buyer1/buyer123");
        }
    }
    
    private void migrateExistingUsers() {
        try {
            // Check if admin user exists with plain text password
            if (userService.findByUsername("admin").isPresent()) {
                User admin = userService.findByUsername("admin").get();
                if (!admin.getPassword().startsWith("$2a$")) { // BCrypt hashes start with $2a$
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    userService.updateUser(admin);
                    System.out.println("Migrated admin password to hashed format");
                }
            }
            
            // Check if seller1 user exists with plain text password
            if (userService.findByUsername("seller1").isPresent()) {
                User seller = userService.findByUsername("seller1").get();
                if (!seller.getPassword().startsWith("$2a$")) {
                    seller.setPassword(passwordEncoder.encode("seller123"));
                    userService.updateUser(seller);
                    System.out.println("Migrated seller1 password to hashed format");
                }
            }
            
            // Check if buyer1 user exists with plain text password
            if (userService.findByUsername("buyer1").isPresent()) {
                User buyer = userService.findByUsername("buyer1").get();
                if (!buyer.getPassword().startsWith("$2a$")) {
                    buyer.setPassword(passwordEncoder.encode("buyer123"));
                    userService.updateUser(buyer);
                    System.out.println("Migrated buyer1 password to hashed format");
                }
            }
        } catch (Exception e) {
            System.err.println("Error during password migration: " + e.getMessage());
        }
    }
}
