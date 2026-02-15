package com.nyihtuun.bentosystem.invoiceservice.controller.exception;

import com.nyihtuun.bentosystem.invoiceservice.domain.exception.InvoiceDomainException;
import com.nyihtuun.bentosystem.invoiceservice.domain.exception.InvoiceErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

import static com.nyihtuun.bentosystem.domain.utility.MessageUtil.INVOICE_ERROR;
import static com.nyihtuun.bentosystem.domain.utility.MessageUtil.toKey;

@Slf4j
@RestControllerAdvice
public class InvoiceGlobalExceptionHandler {
    private final MessageSource messageSource;

    public InvoiceGlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(InvoiceDomainException.class)
    public ResponseEntity<?> handleInvoiceDomainException(InvoiceDomainException exception) {
        String messageKey = INVOICE_ERROR + toKey(exception.getErrorCode().name());
        String message =
                messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
        log.error(
                "InvoiceDomainException occurred. errorCode={}, message={}",
                exception.getErrorCode(),
                message,
                exception
        );


        if (exception.getErrorCode() == InvoiceErrorCode.GENERIC_ACCESS_DENIED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
        }

        return ResponseEntity.badRequest().body(message);
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
