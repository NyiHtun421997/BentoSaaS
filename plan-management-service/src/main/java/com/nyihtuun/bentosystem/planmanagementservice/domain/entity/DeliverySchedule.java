package com.nyihtuun.bentosystem.planmanagementservice.domain.entity;

import com.nyihtuun.bentosystem.domain.entity.AggregateRoot;
import com.nyihtuun.bentosystem.domain.valueobject.Code;
import com.nyihtuun.bentosystem.domain.valueobject.DeliveryScheduleId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.domain.valueobject.PlanMealId;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Getter
public class DeliverySchedule extends AggregateRoot<DeliveryScheduleId> {
    private PlanId planId;
    private Code code;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private ZonedDateTime createAt;
    private List<DeliveryScheduleDetail> deliverySchedules;

    private DeliverySchedule(Builder builder) {
        super.setId(builder.deliveryScheduleId);
        planId = builder.planId;
        code = builder.code;
        periodStart = builder.periodStart;
        periodEnd = builder.periodEnd;
        createAt = builder.createAt;
        deliverySchedules = builder.deliverySchedules;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<PlanMealId> mealFor(LocalDate date) {
        return this.deliverySchedules.stream()
                                     .filter(deliveryScheduleDetail -> deliveryScheduleDetail.getDeliveryDate().equals(date))
                                     .findFirst()
                                     .map(DeliveryScheduleDetail::getPlanMealId);
    }


    public static final class Builder {
        private DeliveryScheduleId deliveryScheduleId;
        private PlanId planId;
        private Code code;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private ZonedDateTime createAt;
        private List<DeliveryScheduleDetail> deliverySchedules;

        private Builder() {
        }

        public Builder deliveryScheduleId(DeliveryScheduleId val) {
            deliveryScheduleId = val;
            return this;
        }

        public Builder planId(PlanId val) {
            planId = val;
            return this;
        }

        public Builder code(Code val) {
            code = val;
            return this;
        }

        public Builder periodStart(LocalDate val) {
            periodStart = val;
            return this;
        }

        public Builder periodEnd(LocalDate val) {
            periodEnd = val;
            return this;
        }

        public Builder createAt(ZonedDateTime val) {
            createAt = val;
            return this;
        }

        public Builder deliverySchedules(List<DeliveryScheduleDetail> val) {
            deliverySchedules = val;
            return this;
        }

        public DeliverySchedule build() {
            return new DeliverySchedule(this);
        }
    }
}
