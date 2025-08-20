package com.flightbooking.userservice.service;

import com.flightbooking.userservice.model.User;
import com.flightbooking.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User authenticateUser(String userId) {
        return userRepository.findByUserId(userId);
    }

    public User getUserById(String userId) {
        return userRepository.findByUserId(userId);
    }

    public User updateTotalBookingValue(String userId, double amount) {
        User user = userRepository.findByUserId(userId);
        if (user != null) {
            user.setTotalBookingValue(user.getTotalBookingValue() + amount);
            return userRepository.save(user);
        }
        return null;
    }

    public User createUser(User user) {
        if (user.getUserId() == null) {
            user.setUserId(UUID.randomUUID().toString());
        }
        if (user.getMemberSince() == null) {
            user.setMemberSince(LocalDateTime.now());
        }
        return userRepository.save(user);
    }
} 