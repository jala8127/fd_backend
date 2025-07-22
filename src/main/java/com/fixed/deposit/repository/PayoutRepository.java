package com.fixed.deposit.repository;

import com.fixed.deposit.model.Payout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, Long> {

    List<Payout> findByUserEmail(String userEmail);
    List<Payout> findByDepositId(Long depositId);

    @Query("SELECT p FROM Payout p WHERE p.deposit.status = 'Closed'")
    List<Payout> findPayoutsWithClosedDeposits();

    @Query("SELECT COALESCE(SUM(p.amountPaid), 0.0) FROM Payout p WHERE p.payoutDate = :date")
    double sumAmountPaidByPayoutDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(p.amountPaid), 0.0) FROM Payout p WHERE p.payoutDate BETWEEN :startDate AND :endDate")
    double sumAmountPaidBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}