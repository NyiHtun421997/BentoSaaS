package com.nyihtuun.bentosystem.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public abstract class BaseEntity<ID> {
    private ID id;
}
