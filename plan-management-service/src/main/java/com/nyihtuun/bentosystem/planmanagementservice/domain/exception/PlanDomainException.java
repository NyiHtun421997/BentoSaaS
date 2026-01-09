package com.nyihtuun.bentosystem.planmanagementservice.domain.exception;

public class PlanDomainException extends RuntimeException {
    public PlanDomainException(String message) {
        super(message);
    }

    public PlanDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
