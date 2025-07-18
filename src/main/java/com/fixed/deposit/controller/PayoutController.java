package com.fixed.deposit.controller;

import com.fixed.deposit.model.Deposits;
import com.fixed.deposit.model.Payout;
import com.fixed.deposit.repository.DepositsRepository;
import com.fixed.deposit.service.PayoutService;
import com.fixed.deposit.repository.PayoutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payouts")
@CrossOrigin(origins = "*")
public class PayoutController {

    @Autowired
    private PayoutRepository payoutRepository;

    @Autowired
    private PayoutService payoutService;

    @Autowired
    private DepositsRepository depositsRepository;

    @GetMapping
    public List<Payout> getAllPayouts() {
        return payoutRepository.findAll();
    }

    @GetMapping("/user/{email}")
    public List<Payout> getPayoutsByUserEmail(@PathVariable String email) {
        return payoutRepository.findByUserEmail(email);
    }

    @GetMapping("/deposit/{depositId}")
    public List<Payout> getPayoutsByDepositId(@PathVariable Long depositId) {
        return payoutRepository.findByDepositId(depositId);
    }

    @PostMapping("/manual")
    public String createManualPayout(@RequestParam Long depositId,
                                     @RequestParam double amount,
                                     @RequestParam String type) {
        try {
            Deposits deposit = depositsRepository.findById(depositId)
                    .orElseThrow(() -> new RuntimeException("Deposit not found"));

            payoutService.createPayout(deposit, type, amount);
            return "Payout created";
        } catch (Exception e) {
            return "Failed to create payout: " + e.getMessage();
        }
    }

    @GetMapping("/closed-deposits")
    public List<Payout> getPayoutsWithClosedDeposits() {
        return payoutRepository.findPayoutsWithClosedDeposits();
    }
}