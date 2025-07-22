package com.fixed.deposit.repository;

import com.fixed.deposit.model.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime; // UPDATED: Import LocalDateTime
import java.util.List;

@Repository
public interface PaymentsRepository extends JpaRepository<Payments, Long> {

    List<Payments> findByUserEmail(String userEmail);

    @Query("SELECT COALESCE(SUM(p.amount), 0.0) FROM Payments p WHERE p.paymentDate = :date AND p.status = :status")
    double sumAmountByPaymentDateAndStatus(@Param("date") LocalDateTime date, @Param("status") String status);

    List<Payments> findTop5ByStatusOrderByPaymentDateDesc(String status);

    // UPDATED: The 'startDate' and 'endDate' parameters are now LocalDateTime
    @Query("SELECT COALESCE(SUM(p.amount), 0.0) FROM Payments p WHERE p.status = :status AND p.paymentDate BETWEEN :startDate AND :endDate")
    double sumAmountByStatusBetweenDates(@Param("status") String status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}