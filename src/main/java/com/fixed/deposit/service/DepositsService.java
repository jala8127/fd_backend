package com.fixed.deposit.service;

import com.fixed.deposit.model.Deposits;
import com.fixed.deposit.model.Schemes;
import com.fixed.deposit.model.User;
import com.fixed.deposit.repository.DepositsRepository;
import com.fixed.deposit.repository.SchemesRepository;
import com.fixed.deposit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class DepositsService {

    @Autowired
    private DepositsRepository depositRepo;

    @Autowired
    private SchemesRepository schemeRepo;

    @Autowired
    private UserRepository userRepo;

    public String createDeposit(Map<String, Object> body) {
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
        double totalInterest = maturityAmount - amount;

        Deposits deposit = new Deposits();
        deposit.setUserEmail(email);
        deposit.setAmount(amount);
        deposit.setScheme(scheme);
        deposit.setInterestRate(interestRate);
        deposit.setTenureMonths(tenureMonths);
        deposit.setStartDate(startDate);
        deposit.setMaturityDate(maturityDate);
        deposit.setMaturityAmount(maturityAmount);
        deposit.setInterestEarned(totalInterest);
        deposit.setPayoutType(scheme.getPayout());
        deposit.setStatus("ACTIVE");

        depositRepo.save(deposit);
        return "Deposit successfully created.";
    }

    public List<Deposits> getDepositsByUser(String email) {

        return depositRepo.findByUserEmail(email);
    }

    public List<Map<String, Object>> getAllDepositsWithUserDetails() {
        List<Deposits> deposits = depositRepo.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Deposits deposit : deposits) {
            Map<String, Object> map = new HashMap<>();
            map.put("depositId", deposit.getId());
            map.put("userEmail", deposit.getUserEmail());
            map.put("amount", deposit.getAmount());
            map.put("interestRate", deposit.getInterestRate());
            map.put("tenureMonths", deposit.getTenureMonths());
            map.put("startDate", deposit.getStartDate());
            map.put("maturityDate", deposit.getMaturityDate());
            map.put("maturityAmount", deposit.getMaturityAmount());
            map.put("payoutType", deposit.getPayoutType());
            map.put("interestEarned", deposit.getInterestEarned());
            map.put("status", deposit.getStatus());

            map.put("schemeName", deposit.getScheme().getSchemeName());

            // Fetch user details from userEmail
            Optional<User> userOpt = userRepo.findByEmail(deposit.getUserEmail());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                map.put("userId", user.getUserId());
                map.put("userName", user.getName());
            } else {
                map.put("userId", null);
                map.put("userName", "Unknown");
            }

            result.add(map);
        }

        return result;
    }

    public Map<String, Object> getUserSummary(String email) {
        List<Deposits> deposits = depositRepo.findByUserEmail(email);

        double totalInvested = deposits.stream()
                .mapToDouble(Deposits::getAmount)
                .sum();

        long activeFds = deposits.stream()
                .filter(d -> "ACTIVE".equalsIgnoreCase(d.getStatus()))
                .count();

        double totalInterest = deposits.stream()
                .mapToDouble(Deposits::getInterestEarned)
                .sum();

        Optional<LocalDate> nextMaturity = deposits.stream()
                .filter(d -> "ACTIVE".equalsIgnoreCase(d.getStatus()))
                .map(Deposits::getMaturityDate)
                .min(Comparator.naturalOrder());

        Map<String, Object> response = new HashMap<>();
        response.put("totalAmount", totalInvested);
        response.put("activeFds", activeFds);
        response.put("interestEarned", totalInterest);
        response.put("upcomingMaturity", nextMaturity.map(LocalDate::toString).orElse(null));

        return response;
    }
}