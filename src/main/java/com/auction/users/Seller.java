package com.auction.users;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "sellers")
@PrimaryKeyJoinColumn(name = "user_id")
public class Seller extends User {

    private String storeName;

    public Seller() {
        super();
    }

    public Seller(String username, String name, String password, java.time.LocalDate dob, String email,
                  int age, String role, String gender, String aadhar, String mobile, String storeName) {
        super(username, name, password, dob, email, age, role, gender, aadhar, mobile);
        this.storeName = storeName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}
