package com.nyihtuun.bentosystem.invoiceservice.controller.exception;

import com.nyihtuun.bentosystem.invoiceservice.domain.exception.InvoiceDomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    public ResponseEntity<?> handle(InvoiceDomainException exception) {
        log.error(
                "InvoiceDomainException occurred. errorCode={}, message={}",
                exception.getErrorCode(),
                exception.getMessage(),
                exception
        );
        String messageKey = INVOICE_ERROR + toKey(exception.getErrorCode().name());
        String message =
                messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
        return ResponseEntity.badRequest().body(message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("Global exception handler caught an exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal server error occurred.");
    }
}
