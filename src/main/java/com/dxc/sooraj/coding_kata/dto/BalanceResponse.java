package com.dxc.sooraj.coding_kata.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BalanceResponse(UUID accountId, BigDecimal balance) {
}
