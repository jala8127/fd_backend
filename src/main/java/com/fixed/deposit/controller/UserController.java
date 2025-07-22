package com.fixed.deposit.controller;

import com.fixed.deposit.model.Kyc;
import com.fixed.deposit.model.User;
import com.fixed.deposit.service.KycService;
import com.fixed.deposit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private KycService kycService;

    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsersWithKycStatus());
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.getUserByEmail(email);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateUser(@RequestBody User updatedUser) {
        String result = userService.updateUserPhone(updatedUser);
        if ("User not found".equals(result)) {
            return ResponseEntity.status(404).body(result);
        } else {
            return ResponseEntity.ok(result);
        }
    }

    @PutMapping("/users/{id}/soft-delete")
    public ResponseEntity<String> softDelete(@PathVariable Long id) {
        String result = userService.softDeleteUser(id);

        if (result.equals("Customer account soft-deleted successfully")) {
            return ResponseEntity.ok(result); // 200 OK
        } else if (result.equals("User has active FD")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result); // 409 Conflict
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result); // 404 Not Found
        }
    }

    @GetMapping("/kyc-status")
    public ResponseEntity<?> getKycStatus(@RequestParam String email) {
        Optional<Kyc> kycOptional = kycService.getKycStatusByEmail(email);

        String status = kycOptional
                .map(Kyc::getStatus) // If Kyc object exists, get its status
                .orElse("NOT_SUBMITTED"); // If not, the status is NOT_SUBMITTED

        return ResponseEntity.ok(Collections.singletonMap("status", status));
    }
    @GetMapping("/profile")
    public ResponseEntity<?> getLoggedInUserProfile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user is logged in.");
        }

        String email = principal.getName();

        return userService.getUserProfileByEmail(email)
                .map(ResponseEntity::ok) // If profile exists, return it with 200 OK
                .orElseGet(() -> ResponseEntity.notFound().build()); // If not found, return 404
    }

}
