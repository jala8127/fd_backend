package com.fixed.deposit.controller;

import com.fixed.deposit.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
@CrossOrigin(origins = "*")
public class OtpController {
    @Autowired
    private MailService mailService;

    @PostMapping("/send")
    public String sendOtp(@RequestParam String email) {
        String otp = mailService.generateOtp();
        mailService.sendOtpEmail(email, otp);
        return otp;
        }
}

