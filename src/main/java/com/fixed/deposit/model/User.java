package com.fixed.deposit.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long user_Id;

    private String name;
    private int age;
    private int ph_no;
    private String email;
    private String role;

    @Column(unique = true)
    private String panNo;

    private String mpin;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
