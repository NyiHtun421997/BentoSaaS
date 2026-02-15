package com.nyihtuun.bentosystem.subscriptionservice.controller.exception;

import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionDomainException;
import com.nyihtuun.bentosystem.subscriptionservice.domain.exception.SubscriptionErrorCode;
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
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
    public ResponseEntity<?> handleSubscriptionDomainException(SubscriptionDomainException exception) {
        String messageKey = SUBSCRIPTION_ERROR + toKey(exception.getErrorCode().name());
        String message =
                messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());

        log.error(
                "SubscriptionDomainException occurred. errorCode={}, message={}",
                exception.getErrorCode(),
                message,
                exception
        );

        if (exception.getErrorCode() == SubscriptionErrorCode.ACCESS_DENIED) {
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

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleNoResourceFoundException(NoResourceFoundException e) {
        log.error("No resource found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The requested resource was not found.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("Global exception handler caught an exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal server error occurred.");
    }
}
