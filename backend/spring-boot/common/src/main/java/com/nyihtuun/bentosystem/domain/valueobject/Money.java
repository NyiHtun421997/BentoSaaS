package com.nyihtuun.bentosystem.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal amount) {
    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public boolean isGreaterThanZero() {
        return this.amount != null && this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public Money add(Money money) {
        return new Money(setScale(this.amount.add(money.amount())));
    }

    public Money subtract(Money money) {
        return new Money(setScale(this.amount.subtract(money.amount())));
    }

    private BigDecimal setScale(BigDecimal input) {
        return input.setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money(BigDecimal amount1))) return false;
        return amount != null && amount1 != null && amount.compareTo(amount1) == 0;
    }

    @Override
    public int hashCode() {
        return amount != null ? amount.stripTrailingZeros().hashCode() : 0;
    }
}
