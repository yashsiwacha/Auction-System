package com.in.service;

import com.in.entity.User;
import com.in.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    public User createUser(String username, String password, String email, 
                          String fullName, String phoneNumber, User.UserRole role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // Hash the password
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);
        user.setRole(role);
        user.setIsActive(true);
        
        return saveUser(user);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findById(Integer id) {
        User user = userRepository.findOne(id);
        return user != null ? Optional.of(user) : Optional.empty();
    }
    
    public List<User> findByRole(User.UserRole role) {
        return userRepository.findByRole(role);
    }
    
    public List<User> findAllActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }
    
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    public void deactivateUser(Integer userId) {
        User user = userRepository.findOne(userId);
        if (user != null) {
            user.setIsActive(false);
            updateUser(user);
        }
    }
    
    public void activateUser(Integer userId) {
        User user = userRepository.findOne(userId);
        if (user != null) {
            user.setIsActive(true);
            updateUser(user);
        }
    }
    
    public boolean validateUser(String username, String password) {
        Optional<User> userOpt = findByUsername(username);
        return userOpt.isPresent() && 
               passwordEncoder.matches(password, userOpt.get().getPassword()) && 
               userOpt.get().getIsActive();
    }
    
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userRepository.findOne(userId);
        if (user != null && passwordEncoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            updateUser(user);
            return true;
        }
        return false;
    }
}
