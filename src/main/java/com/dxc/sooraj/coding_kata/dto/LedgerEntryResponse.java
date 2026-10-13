package com.dxc.sooraj.coding_kata.dto;

import com.dxc.sooraj.coding_kata.entities.LedgerEntry;
import com.dxc.sooraj.coding_kata.enums.EntryType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerEntryResponse(
        UUID entryId,
        UUID transactionId,
        EntryType entryType,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Instant occurredAt) {

    public static LedgerEntryResponse from(LedgerEntry entry) {
        return new LedgerEntryResponse(
                entry.getId(),
                entry.getTransaction().getId(),
                entry.getEntryType(),
                entry.getAmount(),
                entry.getBalanceAfter(),
                entry.getOccurredAt());
    }
}
