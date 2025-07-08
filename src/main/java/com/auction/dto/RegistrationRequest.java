package com.auction.dto;

public class RegistrationRequest {
    private String username;
    private String name;
    private String password;
    private String email;
    private String role; // "ADMIN", "SELLER", "BUYER"
    // You can add more fields as needed

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
