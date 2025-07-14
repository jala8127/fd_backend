package com.fixed.deposit.repository;

import com.fixed.deposit.model.Schemes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchemesRepository extends JpaRepository<Schemes, Long> {
    List<Schemes> findByIsActiveTrue();
}