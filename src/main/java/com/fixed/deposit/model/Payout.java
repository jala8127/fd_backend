package com.fixed.deposit.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "payouts")
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String userEmail;

    private String schemeName;

    @ManyToOne
    @JoinColumn(name = "deposit_id")
    private Deposits deposit;

    private double amountPaid;

    private String payoutType; // MATURITY / PREMATURE / INTEREST_ONLY

    private LocalDate payoutDate;

    private String transactionId;
}