package com.nyihtuun.bentosystem.planmanagementservice.domain.exception;

import lombok.Getter;

@Getter
public class PlanManagementDomainException extends RuntimeException {

    private final PlanManagementErrorCode errorCode;

    public PlanManagementDomainException(PlanManagementErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
