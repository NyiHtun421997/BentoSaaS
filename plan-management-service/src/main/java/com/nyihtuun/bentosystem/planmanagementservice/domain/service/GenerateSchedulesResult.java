package com.nyihtuun.bentosystem.planmanagementservice.domain.service;

import com.nyihtuun.bentosystem.domain.valueobject.PlanId;
import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.DeliverySchedule;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementErrorCode;
import lombok.Getter;

@Getter
public final class GenerateSchedulesResult {
    private final PlanId planId;
    private final boolean success;
    private final PlanManagementErrorCode errorCode;
    private final DeliverySchedule deliverySchedule;

    private GenerateSchedulesResult(Builder builder) {
        planId = builder.planId;
        success = builder.success;
        errorCode = builder.errorCode;
        deliverySchedule = builder.deliverySchedule;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private PlanId planId;
        private boolean success;
        private PlanManagementErrorCode errorCode;
        private DeliverySchedule deliverySchedule;

        private Builder() {
        }

        public Builder planId(PlanId val) {
            planId = val;
            return this;
        }

        public Builder success(boolean val) {
            success = val;
            return this;
        }

        public Builder errorCode(PlanManagementErrorCode val) {
            errorCode = val;
            return this;
        }

        public Builder deliverySchedule(DeliverySchedule val) {
            deliverySchedule = val;
            return this;
        }

        public GenerateSchedulesResult build() {
            return new GenerateSchedulesResult(this);
        }
    }
}
