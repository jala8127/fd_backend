package com.fixed.deposit.controller;

import com.fixed.deposit.model.User;
import com.fixed.deposit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

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
}