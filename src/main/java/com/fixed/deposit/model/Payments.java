package com.fixed.deposit.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payments")
public class Payments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;
    private Long schemeId;
    private String schemeName;
    private Double amount;
    private String paymentMode;
    private String paymentDetails;
    private String transactionId;
    private String status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime paymentDate;

    @PrePersist
    public void onCreate() {
        this.paymentDate = LocalDateTime.now();
    }
}