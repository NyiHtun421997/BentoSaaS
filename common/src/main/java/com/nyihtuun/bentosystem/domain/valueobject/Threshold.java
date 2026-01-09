package com.nyihtuun.bentosystem.domain.valueobject;

import lombok.Getter;

@Getter
public record Threshold(int min) {
    public boolean isMetBy(int currentCount) {
        return currentCount >= min;
    }
}
