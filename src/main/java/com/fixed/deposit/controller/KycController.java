package com.fixed.deposit.controller;

import com.fixed.deposit.model.Kyc;
import com.fixed.deposit.service.KycService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/kyc")
public class KycController {

    @Autowired
    private KycService kycService;

    @GetMapping("/my-status")
    public ResponseEntity<?> getMyKycStatus(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        String email = principal.getName();
        Optional<Kyc> kycOptional = kycService.getKycStatusByEmail(email);

        return kycOptional
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(Collections.singletonMap("status", "NOT_SUBMITTED")));
    }


    @PostMapping(value = "/submit", consumes = { "multipart/form-data" })
    public ResponseEntity<?> submitKyc(
            @RequestPart("kycData") Kyc kycDetails,
            @RequestPart("kycDocument") MultipartFile kycDocument) {
        try {
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

    @PostMapping("/admin-submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')") // Secures the endpoint for admins/employees
    public ResponseEntity<?> adminSubmitKyc(@RequestPart("kycData") Kyc kycData,
                                            @RequestPart("kycDocument") MultipartFile kycDocument) {
        try {
            Kyc savedKyc = kycService.adminSubmitKyc(kycData, kycDocument);
            return ResponseEntity.ok(savedKyc);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error saving the document."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}