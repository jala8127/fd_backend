package com.fixed.deposit.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
public class Schemes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String schemeName;
    private double interestRate;
    private int tenureMonths;

    private double minAmount;
    private String schemeType;

    private boolean isActive;
    private String payout;

    private Double seniorBonusRate;
}