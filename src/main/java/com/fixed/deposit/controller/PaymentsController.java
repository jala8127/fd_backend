package com.fixed.deposit.controller;

import com.fixed.deposit.model.Payments;
import com.fixed.deposit.service.PaymentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentsController {

    @Autowired
    private PaymentsService paymentsService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createPayment(@RequestBody Map<String, Object> body) {
        Map<String, String> response = paymentsService.createPayment(body);
        if ("FAILURE".equalsIgnoreCase(response.get("status"))) {
            return ResponseEntity.internalServerError().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public List<Payments> getAllPayments() {
        return paymentsService.getAllPayments();
    }

    @GetMapping("/user/{email}")
    public List<Payments> getUserPayments(@PathVariable String email) {
        return paymentsService.getUserPayments(email);
    }
}