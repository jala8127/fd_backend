package com.fixed.deposit.controller;

import com.fixed.deposit.model.Kyc;
import com.fixed.deposit.service.KycService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/kyc")
@CrossOrigin("*")
public class KycController {

    @Autowired
    private KycService kycService;

    @PostMapping(value = "/submit", consumes = { "multipart/form-data" })
    public ResponseEntity<?> submitKyc(
            @RequestPart("kycData") Kyc kycDetails,
            @RequestPart("kycDocument") MultipartFile kycDocument) {
        try {
            // This now calls the correct service method
            kycService.submitKyc(kycDetails, kycDocument);
            return ResponseEntity.ok(Collections.singletonMap("message", "KYC Submitted Successfully"));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to save document."));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    public List<Kyc> getPendingKycs() {
        return kycService.getPendingKycs();
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveKyc(@PathVariable Long id) {
        try {
            kycService.approveKyc(id);
            return ResponseEntity.ok(Collections.singletonMap("message", "KYC Approved"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectKyc(@PathVariable Long id, @RequestBody String reason) {
        try {
            kycService.rejectKyc(id, reason);
            return ResponseEntity.ok(Collections.singletonMap("message", "KYC Rejected"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/all")
    public List<Kyc> getAllKyc() {
        return kycService.getAllKycs();
    }

    @GetMapping("/completed")
    public List<Kyc> getCompletedKycs() {
        return kycService.getCompletedKycs();
    }
}
