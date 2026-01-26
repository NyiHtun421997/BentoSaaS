package com.nyihtuun.bentosystem.subscriptionservice.controller.exception;

import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionDomainException;
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

import static com.nyihtuun.bentosystem.domain.utility.MessageUtil.*;

@Slf4j
@RestControllerAdvice
public class SubscriptionGlobalExceptionHandler {
    private final MessageSource messageSource;

    public SubscriptionGlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(SubscriptionDomainException.class)
    public ResponseEntity<?> handle(SubscriptionDomainException exception) {
        log.error(
                "SubscriptionDomainException occurred. errorCode={}, message={}",
                exception.getErrorCode(),
                exception.getMessage(),
                exception
        );
        String messageKey = SUBSCRIPTION_ERROR + toKey(exception.getErrorCode().name());
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
