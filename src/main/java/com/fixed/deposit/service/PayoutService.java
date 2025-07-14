package com.fixed.deposit.service;


import com.fixed.deposit.model.Deposits;
import com.fixed.deposit.model.Payout;
import com.fixed.deposit.repository.PayoutRepository;
import com.fixed.deposit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class PayoutService {
    @Autowired
    private PayoutRepository payoutRepo;

    @Autowired
    private UserRepository userRepo;

    public void createPayout(Deposits deposit, String payoutType, double amountPaid) {
        Payout payout = new Payout();
        payout.setUserEmail(deposit.getUserEmail());
        payout.setUserId(userRepo.findByEmail(deposit.getUserEmail())
                .map(user -> user.getUserId())
                .orElse(null)); // Optional handling
        payout.setSchemeName(deposit.getScheme().getSchemeName());
        payout.setDeposit(deposit);
        payout.setAmountPaid(amountPaid);
        payout.setPayoutType(payoutType); // "MATURITY" or "PREMATURE"
        payout.setPayoutDate(LocalDate.now());
        payout.setTransactionId(UUID.randomUUID().toString());

        payoutRepo.save(payout);
    }
}
