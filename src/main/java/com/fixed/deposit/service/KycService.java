package com.fixed.deposit.service;

import com.fixed.deposit.model.Kyc;
import com.fixed.deposit.model.User;
import com.fixed.deposit.repository.KycRepository;
import com.fixed.deposit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class KycService {

    @Autowired
    private KycRepository kycRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public Kyc submitKyc(Kyc formData, MultipartFile kycDocument) throws IOException {
        User user = userRepository.findByEmail(formData.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + formData.getEmail()));

        Kyc kycToSave = kycRepository.findByUser_UserId(user.getUserId())
                .orElseGet(Kyc::new);

        kycToSave.setUser(user);
        kycToSave.setFullName(formData.getFullName());
        kycToSave.setEmail(formData.getEmail());
        kycToSave.setPhone(formData.getPhone());
        kycToSave.setDob(formData.getDob());
        kycToSave.setCurrentAddress(formData.getCurrentAddress());
        kycToSave.setPermanentAddress(formData.getPermanentAddress());
        kycToSave.setPanNumber(formData.getPanNumber());
        kycToSave.setAadhaarNumber(formData.getAadhaarNumber());
        kycToSave.setBankName(formData.getBankName());
        kycToSave.setAccountNumber(formData.getAccountNumber());
        kycToSave.setIfscCode(formData.getIfscCode());

        kycToSave.setStatus("PENDING");
        kycToSave.setRejectionReason(null);

        if (kycDocument != null && !kycDocument.isEmpty()) {
            String uniqueFileName = saveFile(kycDocument);
            kycToSave.setAadhaarDocument(uniqueFileName);
        }

        return kycRepository.save(kycToSave);
    }

    public Kyc approveKyc(Long id) {
        Kyc kyc = kycRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KYC record not found with ID: " + id));
        kyc.setStatus("APPROVED");
        kyc.setRejectionReason(null);
        return kycRepository.save(kyc);
    }

    public Kyc rejectKyc(Long id, String reason) {
        Kyc kyc = kycRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KYC record not found with ID: " + id));
        kyc.setStatus("REJECTED");
        kyc.setRejectionReason(reason);
        return kycRepository.save(kyc);
    }


    public List<Kyc> getPendingKycs() {
        return kycRepository.findByStatus("PENDING");
    }

    public List<Kyc> getAllKycs() {
        return kycRepository.findAll();
    }

    public List<Kyc> getCompletedKycs() {
        return kycRepository.findByStatusIn(List.of("APPROVED", "REJECTED"));
    }

    public Optional<Kyc> getKycStatusByEmail(String email) {
        return kycRepository.findByUser_Email(email);
    }

    private String saveFile(MultipartFile file) throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : "";

        String uniqueName = UUID.randomUUID() + extension;
        File destination = Paths.get(System.getProperty("user.dir"), uploadDir, uniqueName).toFile();
        destination.getParentFile().mkdirs();
        file.transferTo(destination);
        return uniqueName;
    }
    // Add this new method to your existing KycService class

    public Kyc adminSubmitKyc(Kyc formData, MultipartFile kycDocument) throws IOException {
        // This logic is safe for an admin because it explicitly finds the user by the email in the form
        User user = userRepository.findByEmail(formData.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + formData.getEmail()));

        // The rest of the logic is the same as your submitKyc method
        Kyc kycToSave = kycRepository.findByUser_UserId(user.getUserId())
                .orElseGet(Kyc::new);

        kycToSave.setUser(user);
        kycToSave.setFullName(formData.getFullName());
        kycToSave.setEmail(formData.getEmail());
        kycToSave.setPhone(formData.getPhone());
        kycToSave.setDob(formData.getDob());
        kycToSave.setCurrentAddress(formData.getCurrentAddress());
        kycToSave.setPermanentAddress(formData.getPermanentAddress());
        kycToSave.setPanNumber(formData.getPanNumber());
        kycToSave.setAadhaarNumber(formData.getAadhaarNumber());
        kycToSave.setBankName(formData.getBankName());
        kycToSave.setAccountNumber(formData.getAccountNumber());
        kycToSave.setIfscCode(formData.getIfscCode());

        kycToSave.setStatus("PENDING");
        kycToSave.setRejectionReason(null);

        if (kycDocument != null && !kycDocument.isEmpty()) {
            String uniqueFileName = saveFile(kycDocument);
            kycToSave.setAadhaarDocument(uniqueFileName);
        }

        return kycRepository.save(kycToSave);
    }
}
