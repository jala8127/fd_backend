package com.fixed.deposit.repository;

import com.fixed.deposit.model.Kyc;
import com.fixed.deposit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KycRepository extends JpaRepository<Kyc, Long> {

    List<Kyc> findByStatus(String status);

    Optional<Kyc> findByUser(User user);

    Optional<Kyc> findByUser_Email(String email);

    Optional<Kyc> findByUser_UserId(Long userId);

}