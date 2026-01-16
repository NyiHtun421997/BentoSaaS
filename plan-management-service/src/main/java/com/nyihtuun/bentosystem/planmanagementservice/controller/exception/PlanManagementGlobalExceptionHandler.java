package com.nyihtuun.bentosystem.planmanagementservice.controller.exception;

import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementDomainException;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class PlanManagementGlobalExceptionHandler {

    private static final String PLAN_ERROR = "plan.error";

    private final MessageSource messageSource;

    public PlanManagementGlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(PlanManagementDomainException.class)
    public ResponseEntity<?> handle(PlanManagementDomainException exception) {
        log.error(
                "PlanManagementDomainException occurred. errorCode={}, message={}",
                exception.getErrorCode(),
                exception.getMessage(),
                exception
        );
        String messageKey = PLAN_ERROR + toKey(exception.getErrorCode());
        String message =
                messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
        return ResponseEntity.badRequest().body(message);
    }

    private String toKey(PlanManagementErrorCode code) {
        return code.name().toLowerCase().replace('_', '-');
    }
}
