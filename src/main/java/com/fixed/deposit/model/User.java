package com.fixed.deposit.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String name;
    private String dob;
    private String phone;
    private String email;
    private String role;
    private String mpin;


    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(nullable = true)
    private String activeToken;
}