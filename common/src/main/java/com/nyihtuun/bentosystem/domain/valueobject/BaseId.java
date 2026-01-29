package com.nyihtuun.bentosystem.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public abstract class BaseId<T> {

    @Getter
    private final T value;

    protected BaseId(T value) {
        this.value = value;
    }
}
