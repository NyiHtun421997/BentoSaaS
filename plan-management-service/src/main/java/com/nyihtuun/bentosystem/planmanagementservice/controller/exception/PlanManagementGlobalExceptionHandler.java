package com.nyihtuun.bentosystem.planmanagementservice.controller.exception;

import com.nyihtuun.bentosystem.planmanagementservice.domain.exception.PlanManagementDomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
    public ResponseEntity<?> handle(PlanManagementDomainException exception) {
        log.error(
                "PlanManagementDomainException occurred. errorCode={}, message={}",
                exception.getErrorCode(),
                exception.getMessage(),
                exception
        );
        String messageKey = PLAN_ERROR + toKey(exception.getErrorCode().name());
        String message =
                messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
        return ResponseEntity.badRequest().body(message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleErrors(MethodArgumentNotValidException ex) {
        Map<String, String> response = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                                                               response.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("Global exception handler caught an exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal server error occurred.");
    }
}
