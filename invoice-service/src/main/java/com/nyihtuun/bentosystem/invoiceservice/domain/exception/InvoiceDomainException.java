package com.nyihtuun.bentosystem.invoiceservice.domain.exception;

import lombok.Getter;

@Getter
public class InvoiceDomainException extends RuntimeException {

    private final InvoiceErrorCode errorCode;

    public InvoiceDomainException(InvoiceErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
