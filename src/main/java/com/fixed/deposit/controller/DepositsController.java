package com.fixed.deposit.controller;

import com.fixed.deposit.model.Deposits;
import com.fixed.deposit.service.DepositsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deposits")
public class DepositsController {

    @Autowired
    private DepositsService depositService;

    @PostMapping("/create")
    public ResponseEntity<String> createDeposit(@RequestBody Map<String, Object> body) {
        try {
            String result = depositService.createDeposit(body);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating deposit: " + e.getMessage());
        }
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<?> getDepositsByUser(@PathVariable String email) {
        try {
            return ResponseEntity.ok(depositService.getDepositsByUser(email));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching deposits");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllDeposits() {
        return ResponseEntity.ok(depositService.getAllDepositsWithUserDetails());
    }

    @GetMapping("/summary/{email}")
    public ResponseEntity<Map<String, Object>> getDepositSummary(@PathVariable String email) {
        try {
            return ResponseEntity.ok(depositService.getUserSummary(email));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error calculating summary"));
        }
    }
}