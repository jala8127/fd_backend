package com.fixed.deposit.repository;

import com.fixed.deposit.model.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentsRepository extends JpaRepository<Payments, Long> {

    List<Payments> findByUserEmail(String userEmail);
}