package com.auction.users;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    private LocalDate dob;
    private String email;
    private int age;
    private String role;
    private String gender;
    private String aadhar;
    private String mobile;

    // Constructors
    public User() {}

    public User(String username, String name, String password, LocalDate dob, String email,
                int age, String role, String gender, String aadhar, String mobile) {
        this.username = username;
        this.name = name;
        this.password = password;
        this.dob = dob;
        this.email = email;
        this.age = age;
        this.role = role;
        this.gender = gender;
        this.aadhar = aadhar;
        this.mobile = mobile;
    } 
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAadhar() {
        return aadhar;
    }

    public void setAadhar(String aadhar) {
        this.aadhar = aadhar;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        // Compare by id if present, otherwise by username
        if (id != null && user.id != null) {
            return id.equals(user.id);
        }
        return username != null && username.equals(user.username);
    }

    @Override
    public int hashCode() {
        // Use id if present, otherwise username
        if (id != null) {
            return id.hashCode();
        }
        return username != null ? username.hashCode() : 0;
    }

}
