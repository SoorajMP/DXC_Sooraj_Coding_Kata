package com.dxc.sooraj.coding_kata.entities;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


@Entity
@Table( name = "ACCOUNTS")
@Data
/* Entity class for ORM mapping Accounts Table*/
public class Accounts {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID Id;


    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal balance;

    @Column ( name = "CREATED_AT", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    @Column(nullable = false)
    private Long version;


}
