package com.nyihtuun.bentosystem.subscriptionservice.domain.exception;

import lombok.Getter;

@Getter
public class SubscriptionDomainException extends RuntimeException {

    private final SubscriptionErrorCode errorCode;

    public SubscriptionDomainException(SubscriptionErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
