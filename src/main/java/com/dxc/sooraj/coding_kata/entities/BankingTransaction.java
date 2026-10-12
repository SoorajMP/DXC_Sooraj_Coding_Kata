package com.dxc.sooraj.coding_kata.entities;

import com.dxc.sooraj.coding_kata.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "BANKING_TRANSACTIONS")
@Data
public class BankingTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID Id;

    @Enumerated(EnumType.STRING)
    @Column(name = "TRANSACTION_TYPE", nullable = false, length = 25)
    private TransactionType transactionType;

    @Column(name = "AMOUNT", nullable = false, precision = 25, scale = 2)
    private BigDecimal amount;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SOURCE_ACCOUNT_ID")
    private Accounts sourceAccount;

    @ManyToOne ( fetch = FetchType.LAZY)
    @JoinColumn(name =  "DESTINATION_ACCOUNT_ID")
    private Accounts destinationAccount;

    @Column(name = "OCCURRED_AT", nullable = false, updatable = false)
    private Instant createdAt;


}
