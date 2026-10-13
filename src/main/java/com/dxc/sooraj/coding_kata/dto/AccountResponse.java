package com.dxc.sooraj.coding_kata.dto;

import com.dxc.sooraj.coding_kata.entities.Accounts;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID accountId,
        BigDecimal balance,
        Instant createdAt) {

    public static AccountResponse from(Accounts account) {
        return new AccountResponse(
                account.getId(),
                account.getBalance(),
                account.getCreatedAt());
    }
}
