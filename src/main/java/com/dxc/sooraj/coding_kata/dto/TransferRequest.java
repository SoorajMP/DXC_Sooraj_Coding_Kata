package com.dxc.sooraj.coding_kata.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount) {
}
