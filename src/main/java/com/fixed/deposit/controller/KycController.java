package com.fixed.deposit.controller;

import com.fixed.deposit.model.Kyc;
import com.fixed.deposit.repository.KycRepository;
import com.fixed.deposit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/kyc")
@CrossOrigin("*")
public class KycController {

    @Autowired
    private KycRepository kycRepo;

    @Autowired
    private UserRepository userRepo;

    // Submit KYC
    @PostMapping("/submit")
    public ResponseEntity<?> submitKyc(
            @RequestParam Long userId,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String dob,
            @RequestParam String currentAddress,
            @RequestParam String permanentAddress,
            @RequestParam String panNumber,
            @RequestParam String aadhaarNumber,
            @RequestParam(required = false) MultipartFile aadhaarDocument) {

        return userRepo.findById(userId)
                .map(user -> {
                    Kyc kyc = new Kyc();
                    kyc.setUser(user);
                    kyc.setFullName(fullName);
                    kyc.setEmail(email);
                    kyc.setPhone(phone);
                    kyc.setDob(dob);
                    kyc.setCurrentAddress(currentAddress);
                    kyc.setPermanentAddress(permanentAddress);
                    kyc.setPanNumber(panNumber);
                    kyc.setAadhaarNumber(aadhaarNumber);
                    kyc.setStatus("PENDING");

                    if (aadhaarDocument != null && !aadhaarDocument.isEmpty()) {
                        kyc.setAadhaarDocument(aadhaarDocument.getOriginalFilename());
                        // Optionally store the file to disk or DB
                    }

                    kycRepo.save(kyc);
                    return ResponseEntity.ok(Collections.singletonMap("message", "KYC Submitted"));
                })
                .orElse(ResponseEntity.badRequest().body(Collections.singletonMap("error", "User not found")));
    }
    // Get All Pending KYCs
    @GetMapping("/pending")
    public List<Kyc> getPendingKycs() {
        return kycRepo.findByStatus("PENDING");
    }

    // Approve KYC
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveKyc(@PathVariable Long id) {
        return kycRepo.findById(id)
                .map(kyc -> {
                    kyc.setStatus("APPROVED");
                    kyc.setRejectionReason(null);
                    kycRepo.save(kyc);
                    return ResponseEntity.ok("KYC Approved");
                })
                .orElse(ResponseEntity.badRequest().body("KYC Not Found"));
    }

    // Reject KYC
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectKyc(@PathVariable Long id, @RequestBody String reason) {
        return kycRepo.findById(id)
                .map(kyc -> {
                    kyc.setStatus("REJECTED");
                    kyc.setRejectionReason(reason);
                    kycRepo.save(kyc);
                    return ResponseEntity.ok("KYC Rejected");
                })
                .orElse(ResponseEntity.badRequest().body("KYC Not Found"));
    }

    @GetMapping("/all")
    public List<Kyc> getAllKyc() {
        return kycRepo.findAll();
    }
    // Get Completed KYCs (new)
    @GetMapping("/completed")
    public List<Kyc> getCompletedKycs() {
        return kycRepo.findByStatusIn(List.of("APPROVED", "REJECTED"));
    }
}