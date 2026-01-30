package com.nyihtuun.bentosystem.userservice.exception;

import lombok.Getter;

@Getter
public class UserServiceException extends RuntimeException {
    private final UserServiceErrorCode errorCode;

    public UserServiceException(UserServiceErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
