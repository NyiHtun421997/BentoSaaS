package com.nyihtuun.bentosystem.planmanagementservice.domain.entity;

import com.nyihtuun.bentosystem.domain.entity.BaseEntity;
import com.nyihtuun.bentosystem.domain.valueobject.CategoryId;
import lombok.Getter;

@Getter
public class Category extends BaseEntity<CategoryId> {
    private String name;

    public Category(CategoryId categoryId, String name) {
        super.setId(categoryId);
        capitalizeCategoryName(name);
    }

    private void capitalizeCategoryName(String name) {
        this.name = name.toUpperCase();
    }
}
