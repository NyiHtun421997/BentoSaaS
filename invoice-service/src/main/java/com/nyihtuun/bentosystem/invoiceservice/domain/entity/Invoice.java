package com.nyihtuun.bentosystem.invoiceservice.domain.entity;

import com.nyihtuun.bentosystem.domain.entity.AggregateRoot;
import com.nyihtuun.bentosystem.domain.valueobject.InvoiceId;
import com.nyihtuun.bentosystem.domain.valueobject.Money;
import com.nyihtuun.bentosystem.domain.valueobject.SubscriptionId;
import com.nyihtuun.bentosystem.domain.valueobject.UserId;
import com.nyihtuun.bentosystem.domain.valueobject.status.InvoiceStatus;
import com.nyihtuun.bentosystem.invoiceservice.domain.exception.InvoiceDomainException;
import com.nyihtuun.bentosystem.invoiceservice.domain.exception.InvoiceErrorCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@ToString
public class Invoice extends AggregateRoot<InvoiceId> {
    private final SubscriptionId subscriptionId;
    private final UserId userId;
    private final UserId providedUserId;
    private InvoiceStatus invoiceStatus;
    private final Money amount;
    private LocalDateTime issuedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paidAt;
    private LocalDate periodStart;
    private LocalDate periodEnd;

    private Invoice(Builder builder) {
        super.setId(builder.invoiceId);
        subscriptionId = builder.subscriptionId;
        userId = builder.userId;
        providedUserId = builder.providedUserId;
        invoiceStatus = builder.invoiceStatus;
        amount = builder.amount;
        issuedAt = builder.issuedAt;
        updatedAt = builder.updatedAt;
        paidAt = builder.paidAt;
        periodStart = builder.periodStart;
        periodEnd = builder.periodEnd;
    }

    public void initializeInvoice(LocalDate periodStart, LocalDate periodEnd) {
        super.setId(new InvoiceId(UUID.randomUUID()));
        this.invoiceStatus = InvoiceStatus.ISSUED;
        this.issuedAt = LocalDateTime.now();
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    public void pay() {
        if (this.invoiceStatus == InvoiceStatus.PAID)
            throw new InvoiceDomainException(InvoiceErrorCode.ALREADY_PAID);

        if (this.invoiceStatus == InvoiceStatus.CANCELLED)
            throw new InvoiceDomainException(InvoiceErrorCode.ALREADY_CANCELLED);

        validateExpireTime();

        this.invoiceStatus = InvoiceStatus.PAID;
        this.paidAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        validateExpireTime();
        this.invoiceStatus = InvoiceStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed() {
        validateExpireTime();
        this.invoiceStatus = InvoiceStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    private void validateExpireTime() {
        if (LocalDate.now().isAfter(periodEnd))
            throw new InvoiceDomainException(InvoiceErrorCode.PAYMENT_PERIOD_EXPIRED);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private InvoiceId invoiceId;
        private SubscriptionId subscriptionId;
        private UserId userId;
        private UserId providedUserId;
        private InvoiceStatus invoiceStatus;
        private Money amount;
        private LocalDateTime issuedAt;
        private LocalDateTime updatedAt;
        private LocalDateTime paidAt;
        private LocalDate periodStart;
        private LocalDate periodEnd;

        private Builder() {
        }

        public Builder invoiceId(InvoiceId val) {
            invoiceId = val;
            return this;
        }

        public Builder subscriptionId(SubscriptionId val) {
            subscriptionId = val;
            return this;
        }

        public Builder userId(UserId val) {
            userId = val;
            return this;
        }

        public Builder providedUserId(UserId val) {
            providedUserId = val;
            return this;
        }

        public Builder invoiceStatus(InvoiceStatus val) {
            invoiceStatus = val;
            return this;
        }

        public Builder amount(Money val) {
            amount = val;
            return this;
        }

        public Builder issuedAt(LocalDateTime val) {
            issuedAt = val;
            return this;
        }

        public Builder updatedAt(LocalDateTime val) {
            updatedAt = val;
            return this;
        }

        public Builder paidAt(LocalDateTime val) {
            paidAt = val;
            return this;
        }

        public Builder periodStart(LocalDate val) {
            periodStart = val;
            return this;
        }

        public Builder periodEnd(LocalDate val) {
            periodEnd = val;
            return this;
        }

        public Invoice build() {
            return new Invoice(this);
        }
    }
}
