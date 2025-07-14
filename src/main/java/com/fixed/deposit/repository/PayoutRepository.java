package com.fixed.deposit.repository;

import com.fixed.deposit.model.Payout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
    public interface PayoutRepository extends JpaRepository<Payout, Long> {
        List<Payout> findByUserEmail(String userEmail);
        List<Payout> findByDepositId(Long depositId);
}
