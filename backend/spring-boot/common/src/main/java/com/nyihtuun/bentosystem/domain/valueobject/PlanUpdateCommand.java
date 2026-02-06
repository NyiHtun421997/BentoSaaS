package com.nyihtuun.bentosystem.domain.valueobject;


import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanUpdateCommand {
    private String title;
    private String description;
    private Set<CategoryId> categoryIds;
    private List<LocalDate> skipDays;
    private Address address;
    private Money displaySubscriptionFee;
    private String imageUrl;
}
