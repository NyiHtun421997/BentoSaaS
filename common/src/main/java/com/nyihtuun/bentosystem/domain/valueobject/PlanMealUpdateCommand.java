package com.nyihtuun.bentosystem.domain.valueobject;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanMealUpdateCommand {
    private String name;
    private String description;
    private Money pricePerMonth;

    private boolean isPrimary;
    private Threshold minSubCount;
    private String imageUrl;
}
