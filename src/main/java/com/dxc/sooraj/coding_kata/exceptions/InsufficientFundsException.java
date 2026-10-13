package com.dxc.sooraj.coding_kata.exceptions;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientFundsException extends RuntimeException {

    // This error is used when withdrawal or transfer cannot continue.
    public InsufficientFundsException(UUID accountId, BigDecimal balance, BigDecimal amount) {
        super("Account %s has balance %s, but the requested amount is %s"
                .formatted(accountId, balance, amount));
    }
}
