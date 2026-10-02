package com.licensis.notaire.domain.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Currency helpers for scale-2 monetary amounts (issue #1061 / CU15).
 */
public final class Money {

    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private Money() {
    }

    public static BigDecimal of(String amount) {
        return new BigDecimal(amount).setScale(SCALE, ROUNDING);
    }

    public static BigDecimal of(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.setScale(SCALE, ROUNDING);
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(SCALE);
    }

    public static BigDecimal nullToZero(BigDecimal amount) {
        return amount == null ? zero() : of(amount);
    }
}
