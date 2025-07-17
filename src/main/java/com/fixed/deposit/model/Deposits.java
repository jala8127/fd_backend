package com.fixed.deposit.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "deposits")
public class Deposits {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "scheme_id")
    private Schemes scheme;


    private Double amount;
    private Double interestRate;
    private Integer tenureMonths;
    private LocalDate startDate;
    private LocalDate closeDate;
    private LocalDate maturityDate;
    private Double maturityAmount;
    private String payoutType;
    private double interestEarned;
    private String status;

    public String getSchemeName() {
        return scheme != null ? scheme.getSchemeName() : null;
    }
}