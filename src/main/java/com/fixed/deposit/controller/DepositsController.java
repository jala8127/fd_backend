package com.fixed.deposit.controller;

import com.fixed.deposit.model.Deposits;
import com.fixed.deposit.model.Schemes;
import com.fixed.deposit.repository.DepositsRepository;
import com.fixed.deposit.repository.SchemesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/deposits")
public class DepositsController {

    @Autowired
    private DepositsRepository depositRepo;

    @Autowired
    private SchemesRepository schemeRepo;

    @PostMapping("/create")
    public ResponseEntity<String> createDeposit(@RequestBody Map<String, Object> body) {
        try {
            String email = (String) body.get("userEmail");
            Long schemeId = Long.parseLong(body.get("schemeId").toString());
            Double amount = Double.parseDouble(body.get("amount").toString());

            Schemes scheme = schemeRepo.findById(schemeId)
                    .orElseThrow(() -> new RuntimeException("Scheme not found"));

            double interestRate = scheme.getInterestRate();
            int tenureMonths = scheme.getTenureMonths();
            LocalDate startDate = LocalDate.now();
            LocalDate maturityDate = startDate.plusMonths(tenureMonths);
            double maturityAmount = amount * Math.pow(1 + interestRate / 100, tenureMonths / 12.0);

            Deposits deposit = new Deposits();
            deposit.setUserEmail(email);
            deposit.setScheme(scheme);
            deposit.setAmount(amount);
            deposit.setInterestRate(interestRate);
            deposit.setTenureMonths(tenureMonths);
            deposit.setStartDate(startDate);
            deposit.setMaturityDate(maturityDate);
            deposit.setMaturityAmount(maturityAmount);
            deposit.setPayoutType(scheme.getPayout());
            deposit.setStatus("ACTIVE");

            depositRepo.save(deposit);

            return ResponseEntity.ok("Deposit successfully created.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating deposit: " + e.getMessage());
        }
    }
}