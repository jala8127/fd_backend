package com.fixed.deposit.controller;

import com.fixed.deposit.model.Deposits;
import com.fixed.deposit.model.Payments;
import com.fixed.deposit.model.Schemes;
import com.fixed.deposit.repository.DepositsRepository;
import com.fixed.deposit.repository.PaymentsRepository;
import com.fixed.deposit.repository.SchemesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentsController {

    @Autowired
    private PaymentsRepository paymentRepo;

    @Autowired
    private DepositsRepository depositRepo;

    @Autowired
    private SchemesRepository schemeRepo;

    @PostMapping("/create")
    public ResponseEntity<String> createPayment(@RequestBody Map<String, Object> body) {
        System.out.println("Received Payment: " + body);
        String email = (String) body.get("email");
        Long schemeId = ((Number) body.get("schemeId")).longValue();
        Double amount = ((Number) body.get("amount")).doubleValue();
        String mode = (String) body.get("paymentMode");
        String status = (String) body.get("status");

        // Save payment record
        Payments payment = new Payments();
        payment.setUserEmail(email);
        payment.setSchemeId(schemeId);
        payment.setAmount(amount);
        payment.setPaymentMode(mode);
        payment.setStatus(status);
        payment.setTransactionId(UUID.randomUUID().toString());
        paymentRepo.save(payment);

        if ("SUCCESS".equalsIgnoreCase(status)) {
            Schemes scheme = schemeRepo.findById(schemeId).orElseThrow(() -> new RuntimeException("Scheme not found"));
            double maturity = amount * Math.pow(1 + scheme.getInterestRate() / 100.0, scheme.getTenureMonths() / 12.0);

            Deposits deposit = new Deposits();
            deposit.setUserEmail(email);
            deposit.setScheme(scheme);
            deposit.setAmount(amount);
            deposit.setInterestRate(scheme.getInterestRate());
            deposit.setTenureMonths(scheme.getTenureMonths());
            deposit.setStartDate(LocalDate.now());
            deposit.setMaturityDate(LocalDate.now().plusMonths(scheme.getTenureMonths()));
            deposit.setMaturityAmount(maturity);
            deposit.setPayoutType(scheme.getSchemeType());
            deposit.setStatus("ACTIVE");

            depositRepo.save(deposit);
        }

        return ResponseEntity.ok("Payment processed. Status: " + status);
    }

    @GetMapping("/all")
    public List<Payments> getAllPayments() {
        return paymentRepo.findAll();
    }

    @GetMapping("/user/{email}")
    public List<Payments> getUserPayments(@PathVariable String email) {
        return paymentRepo.findByUserEmail(email);
    }
}