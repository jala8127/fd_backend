package com.fixed.deposit.repository;

import com.fixed.deposit.model.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentsRepository extends JpaRepository<Payments, Long> {

    List<Payments> findByUserEmail(String userEmail);
}