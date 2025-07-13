package com.fixed.deposit.repository;

import com.fixed.deposit.model.Deposits;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositsRepository extends JpaRepository<Deposits, Long> {
}