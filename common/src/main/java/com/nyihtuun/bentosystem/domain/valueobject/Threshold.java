package com.nyihtuun.bentosystem.domain.valueobject;

public record Threshold(int min) {
    public boolean isMetBy(int currentCount) {
        return currentCount >= min;
    }
}
