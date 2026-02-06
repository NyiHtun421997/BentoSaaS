package com.nyihtuun.bentosystem.planmanagementservice.controller.exception;

import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementDomainException;
import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.nyihtuun.bentosystem.domain.utility.MessageUtil.PLAN_ERROR;
import static com.nyihtuun.bentosystem.domain.utility.MessageUtil.toKey;

@Slf4j
@RestControllerAdvice
public class PlanManagementGlobalExceptionHandler {
    private final MessageSource messageSource;

    public PlanManagementGlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(PlanManagementDomainException.class)
    public ResponseEntity<?> handlePlanDomainException(PlanManagementDomainException exception) {
        log.error(
                "PlanManagementDomainException occurred. errorCode={}, message={}",
                exception.getErrorCode(),
                exception.getMessage(),
                exception
        );
        String messageKey = PLAN_ERROR + toKey(exception.getErrorCode().name());
        String message =
                messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());

        if (exception.getErrorCode() == PlanManagementErrorCode.PROVIDER_OR_OWNER_ACCESS_DENIED ||
                exception.getErrorCode() == PlanManagementErrorCode.ADMIN_OR_PROVIDER_ACCESS_DENIED ||
                exception.getErrorCode() == PlanManagementErrorCode.ONLY_PROVIDER_ACCESS_DENIED ||
                exception.getErrorCode() == PlanManagementErrorCode.GENERIC_ACCESS_DENIED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
        }

        return ResponseEntity.badRequest().body(message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> response = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                                                               response.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, String>> handleMissingHeaderException(MissingRequestHeaderException ex) {
        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "error", "MISSING_HEADER",
                        "message", "Required header '" + ex.getHeaderName() + "' is missing"
                ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "error", "MISSING_PARAMETERS",
                        "message", "Required parameters '" + ex.getParameterName() + "' are missing"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("Global exception handler caught an exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal server error occurred.");
    }
}
