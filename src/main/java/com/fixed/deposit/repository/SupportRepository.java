package com.fixed.deposit.repository;

import com.fixed.deposit.model.Support;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupportRepository extends JpaRepository<Support, Long> {
    List<Support> findByStatus(String status);
}