package com.nyihtuun.bentosystem.planmanagementservice.domain.entity;

import com.nyihtuun.bentosystem.domain.entity.BaseEntity;
import com.nyihtuun.bentosystem.domain.valueobject.DeliveryScheduleDetailId;
import com.nyihtuun.bentosystem.domain.valueobject.DeliveryScheduleId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class DeliveryScheduleDetail extends BaseEntity<DeliveryScheduleDetailId> {
    private DeliveryScheduleId deliveryScheduleId;
    private PlanMealId planMealId;
    private LocalDate deliveryDate;

    private DeliveryScheduleDetail(Builder builder) {
        super.setId(builder.deliveryScheduleDetailId);
        deliveryScheduleId = builder.deliveryScheduleId;
        planMealId = builder.planMealId;
        deliveryDate = builder.deliveryDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private DeliveryScheduleDetailId deliveryScheduleDetailId;
        private DeliveryScheduleId deliveryScheduleId;
        private PlanMealId planMealId;
        private LocalDate deliveryDate;

        private Builder() {
        }

        public Builder deliveryScheduleDetailId(DeliveryScheduleDetailId val) {
            deliveryScheduleDetailId = val;
            return this;
        }

        public Builder deliveryScheduleId(DeliveryScheduleId val) {
            deliveryScheduleId = val;
            return this;
        }

        public Builder planMealId(PlanMealId val) {
            planMealId = val;
            return this;
        }

        public Builder deliveryDate(LocalDate val) {
            deliveryDate = val;
            return this;
        }

        public DeliveryScheduleDetail build() {
            return new DeliveryScheduleDetail(this);
        }
    }
}
