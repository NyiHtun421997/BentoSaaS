package com.nyihtuun.bentosystem.subscriptionservice.security.authorization_handler;

import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionDomainException;
import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionErrorCode;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.authorization.method.MethodAuthorizationDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionServiceAccessDeniedHandler implements MethodAuthorizationDeniedHandler {
    @Override
    public @Nullable Object handleDeniedInvocation(@NonNull MethodInvocation methodInvocation, @NonNull AuthorizationResult authorizationResult) {
        throw new SubscriptionDomainException(SubscriptionErrorCode.ACCESS_DENIED);
    }
}
