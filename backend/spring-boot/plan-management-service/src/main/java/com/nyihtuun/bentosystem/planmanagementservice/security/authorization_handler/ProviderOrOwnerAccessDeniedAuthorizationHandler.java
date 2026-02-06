package com.nyihtuun.bentosystem.planmanagementservice.security.authorization_handler;

import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementDomainException;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementErrorCode;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.authorization.method.MethodAuthorizationDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ProviderOrOwnerAccessDeniedAuthorizationHandler implements MethodAuthorizationDeniedHandler {
    @Override
    public @Nullable Object handleDeniedInvocation(@NonNull MethodInvocation methodInvocation, @NonNull AuthorizationResult authorizationResult) {
        throw new PlanManagementDomainException(PlanManagementErrorCode.PROVIDER_OR_OWNER_ACCESS_DENIED);
    }
}
