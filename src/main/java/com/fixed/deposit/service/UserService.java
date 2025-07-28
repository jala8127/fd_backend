package com.fixed.deposit.service;

import com.fixed.deposit.model.Kyc;
import com.fixed.deposit.model.User;
import com.fixed.deposit.repository.DepositsRepository;
import com.fixed.deposit.repository.KycRepository;
import com.fixed.deposit.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KycRepository kycRepository;

    @Autowired
    private DepositsRepository depositsRepository;

    public boolean phoneExists(String phone) {
        return userRepository.existsByPhone(phone);
    }

    public List<Map<String, Object>> getAllUsersWithKycStatus() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : users) {
            if ("DELETED".equalsIgnoreCase(user.getStatus())) {
                continue; // skip deleted users
            }

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userId", user.getUserId());
            userMap.put("name", user.getName());
            userMap.put("email", user.getEmail());
            userMap.put("phone", user.getPhone());

            Optional<Kyc> kyc = kycRepository.findByUser_Email(user.getEmail());
            userMap.put("status", kyc.map(Kyc::getStatus).orElse("Not Submitted"));
            userMap.put("address", kyc.map(Kyc::getCurrentAddress).orElse("N/A"));

            boolean hasActiveFd = depositsRepository.existsByUserEmailAndStatus(user.getEmail(), "ACTIVE");
            userMap.put("activeFd", hasActiveFd ? "Yes" : "No");

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
    public String softDeleteUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return "User not found";

        User user = userOpt.get();

        boolean hasActiveFd = depositsRepository.existsByUserEmailAndStatus(user.getEmail(), "ACTIVE");

        if (hasActiveFd) {
            return "User has active FD";
        }

        user.setStatus("DELETED");
        userRepository.save(user);
        return "Customer account soft-deleted successfully";
    }
    public boolean isUserValid(String email, String mpin) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        return user.getMpin().equals(mpin) && !"DELETED".equals(user.getStatus());
    }
    public Optional<Map<String, Object>> getUserProfileByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        Map<String, Object> userProfile = new HashMap<>();

        userProfile.put("userId", user.getUserId());
        userProfile.put("name", user.getName());
        userProfile.put("email", user.getEmail());
        userProfile.put("phone", user.getPhone());
        userProfile.put("dob", user.getDob());

        Optional<Kyc> kycOpt = kycRepository.findByUser_Email(user.getEmail());
        userProfile.put("status", kycOpt.map(Kyc::getStatus).orElse("Not Submitted"));
        userProfile.put("address", kycOpt.map(Kyc::getCurrentAddress).orElse("N/A"));

        userProfile.put("bankName", kycOpt.map(Kyc::getBankName).orElse("N/A"));
        userProfile.put("bankAccNo", kycOpt.map(Kyc::getAccountNumber).orElse("N/A"));
        userProfile.put("bankIfsc", kycOpt.map(Kyc::getIfscCode).orElse("N/A"));


        boolean hasActiveFd = depositsRepository.existsByUserEmailAndStatus(user.getEmail(), "ACTIVE");
        userProfile.put("activeFd", hasActiveFd ? "Yes" : "No");

        return Optional.of(userProfile);
    }
    @Transactional // <-- Good practice to make this atomic
    public boolean updateUserField(String email, String field, Object value) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false; // User not found
        }
        User user = userOpt.get();

        // Use a switch to handle different fields
        switch (field) {
            case "phone":
                user.setPhone((String) value);
                userRepository.save(user);
                return true;

            // These fields are part of the Kyc entity
            case "address":
            case "bankName":
            case "bankAccNo":
            case "bankIfsc":
                Optional<Kyc> kycOpt = kycRepository.findByUser_Email(email);
                if (kycOpt.isEmpty()) {
                    // You might want to create a new Kyc entity if one doesn't exist
                    // For now, we'll treat it as a failure.
                    return false;
                }
                Kyc kyc = kycOpt.get();
                switch (field) {
                    case "address":
                        kyc.setCurrentAddress((String) value);
                        break;
                    case "bankName":
                        kyc.setBankName((String) value);
                        break;
                    case "bankAccNo":
                        kyc.setAccountNumber((String) value);
                        break;
                    case "bankIfsc":
                        kyc.setIfscCode((String) value);
                        break;
                }
                kycRepository.save(kyc);
                return true;

            default:
                // Field is not editable or does not exist
                throw new IllegalArgumentException("Field '" + field + "' cannot be updated.");
        }
    }
}