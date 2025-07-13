package com.fixed.deposit.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.fixed.deposit.model.Deposits;

public interface DepositsRepository extends JpaRepository<Deposits, Long> {
    List<Deposits> findByUserEmail(String email);

}