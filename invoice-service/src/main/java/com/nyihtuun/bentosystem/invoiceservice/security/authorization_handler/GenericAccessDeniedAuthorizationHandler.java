package com.nyihtuun.bentosystem.invoiceservice.security.authorization_handler;

import com.nyihtuun.bentosystem.invoiceservice.domain.exception.InvoiceDomainException;
import com.nyihtuun.bentosystem.invoiceservice.domain.exception.InvoiceErrorCode;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.authorization.method.MethodAuthorizationDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class GenericAccessDeniedAuthorizationHandler implements MethodAuthorizationDeniedHandler {
    @Override
    public @Nullable Object handleDeniedInvocation(@NonNull MethodInvocation methodInvocation, @NonNull AuthorizationResult authorizationResult) {
        throw new InvoiceDomainException(InvoiceErrorCode.GENERIC_ACCESS_DENIED);
    }
}
