package com.fixed.deposit.controller;

import com.fixed.deposit.model.Deposits;
import com.fixed.deposit.service.DepositsService;
import com.fixed.deposit.service.PaymentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deposits")
public class DepositsController {

    @Autowired
    private DepositsService depositService;

    @Autowired
    private PaymentsService paymentsService;

    @GetMapping("/my-deposits")
    public ResponseEntity<?> getMyDeposits(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        String email = principal.getName();
        try {
            return ResponseEntity.ok(depositService.getDepositsByUser(email));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching deposits");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createDepositAndPayment(@RequestBody Map<String, Object> body, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not authenticated."));
        }
        String userEmail = principal.getName();
        body.put("userEmail", userEmail);

        try {
            paymentsService.createPayment(body);
            return ResponseEntity.ok(Map.of("message", "Deposit created successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error creating deposit: " + e.getMessage()));
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

    @PutMapping("/close/{id}")
    public ResponseEntity<?> closeFd(@PathVariable Long id) {
        try {
            Map<String, Object> response = depositService.closeDeposit(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error closing FD: " + e.getMessage());
        }
    }
    @GetMapping("/preview-close/{id}")
    public ResponseEntity<Map<String, Object>> previewFdClosure(@PathVariable Long id) {
        return ResponseEntity.ok(depositService.previewCloseDeposit(id));
    }
    @GetMapping("/closed/{email}")
    public ResponseEntity<?> getClosedDeposits(@PathVariable String email) {
        try {
            return ResponseEntity.ok(depositService.getClosedDepositsByUser(email));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching closed deposits");
        }
    }
}