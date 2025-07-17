package com.fixed.deposit.service;

import com.fixed.deposit.model.Deposits;
import com.fixed.deposit.model.Payments;
import com.fixed.deposit.model.Schemes;
import com.fixed.deposit.repository.DepositsRepository;
import com.fixed.deposit.repository.PaymentsRepository;
import com.fixed.deposit.repository.SchemesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;


import java.time.LocalDate;
import java.util.*;

@Service
public class PaymentsService {

    @Autowired
    private PaymentsRepository paymentRepo;

    @Autowired
    private DepositsRepository depositRepo;

    @Autowired
    private SchemesRepository schemeRepo;

    public Map<String, String> createPayment(Map<String, Object> body) {
        try {
            String email = (String) body.get("email");
            Long schemeId = ((Number) body.get("schemeId")).longValue();
            String schemeName = (String) body.get("schemeName");
            Double amount = ((Number) body.get("amount")).doubleValue();
            String mode = (String) body.get("paymentMode");
            String status = (String) body.get("status");
            String paymentDetails = (String) body.get("paymentDetails");

            Payments payment = new Payments();
            payment.setUserEmail(email);
            payment.setSchemeId(schemeId);
            payment.setSchemeName(schemeName);
            payment.setAmount(amount);
            payment.setPaymentMode(mode);
            payment.setStatus(status);
            payment.setTransactionId(UUID.randomUUID().toString());
            payment.setPaymentDate(LocalDateTime.now());
            payment.setPaymentDetails(paymentDetails);
            paymentRepo.save(payment);

            if ("SUCCESS".equalsIgnoreCase(status)) {
                Schemes scheme = schemeRepo.findById(schemeId)
                        .orElseThrow(() -> new RuntimeException("Scheme not found"));

                double maturity = amount * Math.pow(1 + scheme.getInterestRate() / 100.0,
                        scheme.getTenureMonths() / 12.0);

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

            return Map.of(
                    "message", "Payment processed successfully",
                    "status", status
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "message", "Error processing payment",
                    "status", "FAILURE"
            );
        }
    }

    public List<Payments> getAllPayments() {
        return paymentRepo.findAll();
    }

    public List<Payments> getUserPayments(String email) {
        return paymentRepo.findByUserEmail(email);
    }
}