package com.dxc.sooraj.coding_kata.services;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class MoneyValidator {

    // Same precision and scale are used in database columns.
    private static final int MAX_PRECISION = 19;
    private static final int SCALE = 2;

    private MoneyValidator() {
    }

    static BigDecimal requireNonNegative(BigDecimal value, String fieldName) {
        BigDecimal checkedValue = normalize(value, fieldName);

        if (checkedValue.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }

        return checkedValue;
    }

    static BigDecimal requirePositive(BigDecimal value) {
        BigDecimal checkedValue = normalize(value, "Amount");

        if (checkedValue.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        return checkedValue;
    }

    private static BigDecimal normalize(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        final BigDecimal checkedValue;
        try {
            checkedValue = value.setScale(SCALE, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(fieldName + " can have maximum two decimal places");
        }

        if (checkedValue.precision() > MAX_PRECISION) {
            throw new IllegalArgumentException(fieldName + " is too large");
        }

        return checkedValue;
    }
}
