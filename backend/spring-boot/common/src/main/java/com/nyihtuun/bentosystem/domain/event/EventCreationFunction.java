package com.nyihtuun.bentosystem.domain.event;

import com.google.protobuf.Message;

@FunctionalInterface
public interface EventCreationFunction<T, U, V, R extends Message> {
    R execute(T t, U u, V v);
}
