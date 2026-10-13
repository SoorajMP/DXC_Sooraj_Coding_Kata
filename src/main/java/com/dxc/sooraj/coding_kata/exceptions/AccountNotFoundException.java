package com.dxc.sooraj.coding_kata.exceptions;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {

    // Keep account id in message to find the failed request easily.
    public AccountNotFoundException(UUID accountId) {
        super("Account not found: " + accountId);
    }
}
