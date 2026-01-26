package com.nyihtuun.bentosystem.invoiceservice.controller;

import com.nyihtuun.bentosystem.invoiceservice.application_service.ports.input.service.InvoiceService;
import com.nyihtuun.bentosystem.invoiceservice.domain.entity.Invoice;
import com.nyihtuun.bentosystem.invoiceservice.domain.exception.InvoiceDomainException;
import com.nyihtuun.bentosystem.invoiceservice.domain.exception.InvoiceErrorCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.nyihtuun.bentosystem.invoiceservice.controller.ApiPaths.INVOICE_ID;
import static com.nyihtuun.bentosystem.invoiceservice.controller.ApiPaths.VERSION1;

@Slf4j
@RestController
@RequestMapping(VERSION1)
@Tag(name = "Plan Query", description = "Endpoints for searching and retrieving bento plans.")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping(INVOICE_ID)
    public ResponseEntity<Invoice> findInvoiceById(@PathVariable UUID invoiceId) {
        log.info("Fetching invoice with id: {}", invoiceId);
        return invoiceService.getInvoiceById(invoiceId)
                .map(invoice -> {
                    log.info("Invoice with id: {} : {}", invoiceId, invoice);
                    return ResponseEntity.ok(invoice);
                })
                .orElseThrow(() -> {
                    log.error("Invoice with id: {} not found", invoiceId);
                    return new InvoiceDomainException(InvoiceErrorCode.INVOICE_NOT_FOUND);
                });
    }

    @GetMapping("byuseridanddate")
    public ResponseEntity<List<Invoice>> findInvoicesByUserIdAndDate(
            @RequestParam UUID userId,
            @RequestParam LocalDate date) {
        log.info("Fetching invoices for user with id: {} on date: {}", userId, date);
        List<Invoice> invoices = invoiceService.getMyInvoicesByDate(userId, date);
        log.info("Found {} invoices for user with id: {} on date: {}", invoices.size(), userId, date);
        return ResponseEntity.ok(invoices);
    }

    @PutMapping("/pay" + INVOICE_ID)
    public ResponseEntity<Invoice> makePayment(@PathVariable UUID invoiceId) {
        log.info("Making payment for invoice with id: {}", invoiceId);
        return ResponseEntity.ok(invoiceService.makePayment(invoiceId));
    }

    @PutMapping("/cancel" + INVOICE_ID)
    public ResponseEntity<Invoice> cancelPayment(@PathVariable UUID invoiceId) {
        log.info("Cancelling payment for invoice with id: {}", invoiceId);
        return ResponseEntity.ok(invoiceService.cancelPayment(invoiceId));
    }

    @PutMapping("/fail" + INVOICE_ID)
    public ResponseEntity<Invoice> markPaymentFailed(@PathVariable UUID invoiceId) {
        log.info("Marking payment for invoice with id: {} as failed", invoiceId);
        return ResponseEntity.ok(invoiceService.markPaymentFailed(invoiceId));
    }
}
