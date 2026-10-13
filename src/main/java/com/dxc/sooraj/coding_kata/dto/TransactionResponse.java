package com.dxc.sooraj.coding_kata.dto;

import com.dxc.sooraj.coding_kata.entities.BankingTransaction;
import com.dxc.sooraj.coding_kata.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID transactionId,
        TransactionType transactionType,
        BigDecimal amount,
        UUID sourceAccountId,
        UUID destinationAccountId,
        Instant occurredAt) {

    public static TransactionResponse from(BankingTransaction transaction) {
        UUID sourceId = transaction.getSourceAccount() == null
                ? null : transaction.getSourceAccount().getId();
        UUID destinationId = transaction.getDestinationAccount() == null
                ? null : transaction.getDestinationAccount().getId();

        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                sourceId,
                destinationId,
                transaction.getCreatedAt());
    }
}
