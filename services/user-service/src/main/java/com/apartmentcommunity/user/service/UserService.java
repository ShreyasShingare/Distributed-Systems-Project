package com.apartmentcommunity.user.service;

import com.apartmentcommunity.user.model.User;
import com.apartmentcommunity.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(String username, String password) {
        return registerUser(username, password, null, null, null);
    }

    public User registerUser(String username, String password, String name, String flatNo, String contactNumber) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Store password in plain text (NOT RECOMMENDED FOR PRODUCTION)
        User user = new User(username, password);
        user.setName(name);
        user.setFlatNo(flatNo);
        user.setContactNumber(contactNumber);
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean validatePassword(String rawPassword, String storedPassword) {
        // Plain text comparison (NOT RECOMMENDED FOR PRODUCTION)
        return rawPassword != null && rawPassword.equals(storedPassword);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}

