package com.fixed.deposit.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Kyc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User user;

    private String fullName;
    private String email;
    private String phone;
    private String dob;
    private String currentAddress;
    private String permanentAddress;

    private String aadhaarNumber;
    private String panNumber;
    private String aadhaarDocument;

    private String status;
    private String rejectionReason;
}