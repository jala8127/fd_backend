package com.fixed.deposit.service;

import com.fixed.deposit.model.Kyc;
import com.fixed.deposit.model.User;
import com.fixed.deposit.repository.KycRepository;
import com.fixed.deposit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KycRepository kycRepository;

    // Get all users with their KYC status
    public List<Map<String, Object>> getAllUsersWithKycStatus() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : users) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userId", user.getUserId());
            userMap.put("name", user.getName());
            userMap.put("email", user.getEmail());
            userMap.put("phone", user.getPhone());

            Optional<Kyc> kyc = kycRepository.findByUser_Email(user.getEmail());
            userMap.put("status", kyc.map(Kyc::getStatus).orElse("Not Submitted"));
            userMap.put("address", kyc.map(Kyc::getCurrentAddress).orElse("N/A"));

            result.add(userMap);
        }

        return result;
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public String updateUserPhone(User updatedUser) {
        Optional<User> userOpt = userRepository.findByEmail(updatedUser.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPhone(updatedUser.getPhone());
            userRepository.save(user);
            return "User updated successfully";
        } else {
            return "User not found";
        }
    }
}