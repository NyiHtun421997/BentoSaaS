package com.nyihtuun.bentosystem.userservice.security.authorization_handler;

import com.nyihtuun.bentosystem.userservice.exception.UserServiceErrorCode;
import com.nyihtuun.bentosystem.userservice.exception.UserServiceException;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.authorization.method.MethodAuthorizationDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class AdminAccessDeniedAuthorizationHandler implements MethodAuthorizationDeniedHandler {
    @Override
    public @Nullable Object handleDeniedInvocation(@NonNull MethodInvocation methodInvocation, @NonNull AuthorizationResult authorizationResult) {
        throw new UserServiceException(UserServiceErrorCode.ADMIN_ACCESS_DENIED);
    }
}
