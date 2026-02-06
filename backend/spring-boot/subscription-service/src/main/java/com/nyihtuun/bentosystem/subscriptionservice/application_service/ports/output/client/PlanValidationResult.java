package com.nyihtuun.bentosystem.subscriptionservice.application_service.ports.output.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlanValidationResult<T> {
    public enum PlanValidationStatus {
        INVALID_PLAN, VALID_PLAN, API_FAILURE
    }

    private PlanValidationStatus planValidationStatus;
    private T data;
}
