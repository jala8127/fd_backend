package com.fixed.deposit.repository;

import com.fixed.deposit.model.Deposits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepositsRepository extends JpaRepository<Deposits, Long> {

    List<Deposits> findByUserEmail(String email);
    boolean existsByUserEmailAndStatus(String email, String status);
    List<Deposits> findByUserEmailAndStatus(String email, String status);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(d.amount), 0.0) FROM Deposits d WHERE d.status = :status")
    double sumAmountByStatus(@Param("status") String status);
}