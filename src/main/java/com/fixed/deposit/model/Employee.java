package com.fixed.deposit.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emp_Id;

    private String name;
    private String email;
    private String phone;
    private String department;
    private String role;
    private String password;

    @Lob
    @Column(nullable = true)
    private String photoUrl;

    @Column(nullable = true)
    private String activeToken;



}