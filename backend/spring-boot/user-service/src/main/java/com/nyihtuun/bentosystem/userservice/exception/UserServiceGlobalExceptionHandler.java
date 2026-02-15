package com.nyihtuun.bentosystem.userservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.nyihtuun.bentosystem.domain.utility.MessageUtil.USER_ERROR;
import static com.nyihtuun.bentosystem.domain.utility.MessageUtil.toKey;

@Slf4j
@RestControllerAdvice
public class UserServiceGlobalExceptionHandler {
    private final MessageSource messageSource;

    public UserServiceGlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<?> handleUserServiceException(UserServiceException exception) {
        String messageKey = USER_ERROR + toKey(exception.getErrorCode().name());
        String message =
                messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());

        log.error(
                "UserServiceException occurred. errorCode={}, message={}",
                exception.getErrorCode(),
                message,
                exception
        );

        if (exception.getErrorCode() == UserServiceErrorCode.GENERIC_ACCESS_DENIED ||
                exception.getErrorCode() == UserServiceErrorCode.ADMIN_ACCESS_DENIED) {
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
