package com.auction.users;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "user_id")
public class Admin extends User {

    private String adminLevel;

    public Admin() {
        super();
    }

    public Admin(String username, String name, String password, java.time.LocalDate dob, String email,
                 int age, String role, String gender, String aadhar, String mobile, String adminLevel) {
        super(username, name, password, dob, email, age, role, gender, aadhar, mobile);
        this.adminLevel = adminLevel;
    }

    public String getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(String adminLevel) {
        this.adminLevel = adminLevel;
    }
}
