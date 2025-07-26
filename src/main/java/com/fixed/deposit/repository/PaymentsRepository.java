package com.fixed.deposit.repository;

import com.fixed.deposit.model.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentsRepository extends JpaRepository<Payments, Long> {

    public interface ChartDataProjection {
        String getMonth();
        double getTotalPayments();
        double getTotalPayouts();
    }

    @Query(value = """
        WITH RECURSIVE months AS (
            SELECT 0 AS n
            UNION ALL
            SELECT n + 1 FROM months WHERE n < 4
        ),
        month_series AS (
            SELECT DATE_FORMAT(NOW() - INTERVAL m.n MONTH, '%Y-%m') AS month_key,
                   UPPER(DATE_FORMAT(NOW() - INTERVAL m.n MONTH, '%b')) AS month_name
            FROM months m
        )
        SELECT
            ms.month_name AS month,
            COALESCE(p.total_payments, 0) AS totalPayments,
            COALESCE(po.total_payouts, 0) AS totalPayouts
        FROM month_series ms
        LEFT JOIN (
            SELECT DATE_FORMAT(payment_date, '%Y-%m') AS month, SUM(amount) AS total_payments
            FROM payments
            WHERE status = 'SUCCESS'
            GROUP BY month
        ) p ON ms.month_key = p.month
        LEFT JOIN (
            -- FINAL FIX: Removed the line "WHERE status = 'PAID'" which was causing the error.
            SELECT DATE_FORMAT(payout_date, '%Y-%m') AS month, SUM(amount_paid) AS total_payouts
            FROM payouts
            GROUP BY month
        ) po ON ms.month_key = po.month
        ORDER BY STR_TO_DATE(CONCAT(ms.month_key, '-01'), '%Y-%m-%d') ASC
    """, nativeQuery = true)
    List<ChartDataProjection> getMonthlyChartData();

    @Query("SELECT COALESCE(SUM(p.amount), 0.0) FROM Payments p WHERE p.status = :status AND p.paymentDate BETWEEN :startDate AND :endDate")
    double sumAmountByStatusBetweenDates(@Param("status") String status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<Payments> findByUserEmail(String userEmail);

    List<Payments> findTop5ByStatusOrderByPaymentDateDesc(String status);
}