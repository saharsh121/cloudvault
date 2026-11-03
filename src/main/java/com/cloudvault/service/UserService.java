package com.cloudvault.service;

import com.cloudvault.model.User;
import com.cloudvault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    // Login user by email and password
    public User loginUser(String email, String password) throws Exception {
        System.out.println("Attempting login with email: " + email);
        
        // Get ALL users from database
        System.out.println("All users in database:");
        for (User u : userRepository.findAll()) {
            System.out.println("   - Email: '" + u.getEmail() + "' | Name: " + u.getName());
        }
        
        // Find user by email
        User user = userRepository.findByEmail(email);
        
        if (user == null) {
            System.out.println("User not found with email: " + email);
            throw new Exception("User not found!");
        }
        
        System.out.println("User found: " + user.getEmail());
        
        if (!user.getPassword().equals(password)) {
            System.out.println("Password mismatch!");
            throw new Exception("Invalid password!");
        }
        
        System.out.println("Login successful!");
        return user;
    }
}