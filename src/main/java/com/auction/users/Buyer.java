package com.auction.users;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "buyers")
@PrimaryKeyJoinColumn(name = "user_id")
public class Buyer extends User {

    private int loyaltyPoints;

    public Buyer() {
        super();
    }

    public Buyer(String username, String name, String password, java.time.LocalDate dob, String email,
                 int age, String role, String gender, String aadhar, String mobile, int loyaltyPoints) {
        super(username, name, password, dob, email, age, role, gender, aadhar, mobile);
        this.loyaltyPoints = loyaltyPoints;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }
}
