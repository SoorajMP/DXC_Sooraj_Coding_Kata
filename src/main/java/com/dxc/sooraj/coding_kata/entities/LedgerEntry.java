package com.dxc.sooraj.coding_kata.entities;

import com.dxc.sooraj.coding_kata.enums.EntryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "LEDGER_ENTRIES")
@Data
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ACCOUNT_ID", nullable = false)
    private Accounts account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TRANSACTION_ID", nullable = false)
    private BankingTransaction transaction;

    @Enumerated(EnumType.STRING)
    @Column(name = "ENTRY_TYPE", nullable = false, length = 10)
    private EntryType entryType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "BALANCE_AFTER", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "OCCURRED_AT", nullable = false, updatable = false)
    private Instant occurredAt;



}