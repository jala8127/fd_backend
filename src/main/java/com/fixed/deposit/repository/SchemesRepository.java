package com.fixed.deposit.repository;

import com.fixed.deposit.model.Schemes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchemesRepository extends JpaRepository<Schemes, Long> {
    List<Schemes> findByIsActiveTrue();
}