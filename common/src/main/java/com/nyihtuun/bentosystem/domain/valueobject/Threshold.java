package com.nyihtuun.bentosystem.domain.valueobject;

public record Threshold(int min) {
    public boolean isMetBy(int currentCount) {
        return currentCount >= min;
    }

    public boolean isGreaterThanZero() {
        return min > 0;
    }

    public boolean isNegative() {
        return min < 0;
    }
}
