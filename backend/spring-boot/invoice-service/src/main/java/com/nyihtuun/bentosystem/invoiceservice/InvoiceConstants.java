package com.nyihtuun.bentosystem.invoiceservice;

public class InvoiceConstants {
    public static final String INVOICE_ITEM_PROCESSOR = "InvoiceItemProcessor";
    public static final String INVOICE_ITEM_READER = "InvoiceItemReader";

    public static final String INVOICE_SCHEDULER_CRON = "${invoice.scheduler-cron}";
    public static final String INVOICE_ZONE = "${invoice.zone}";
    public static final String PLAN_MANAGEMENT_SERVICE_ADDRESS = "${plan-management.service.address}";
    public static final String PLAN_MANAGEMENT_SERVICE_PORT = "${plan-management.service.port}";
    public static final String SUBSCRIPTION_SERVICE_ADDRESS = "${subscription.service.address}";
    public static final String SUBSCRIPTION_SERVICE_PORT = "${subscription.service.port}";
    public static final String SCHEDULER_FIXED_RATE = "${invoice.outbox-scheduler-fixed-rate}";
    public static final String SCHEDULER_INITIAL_DELAY = "${invoice.outbox-scheduler-initial-delay}";
    public static final String IDEMPOTENCY_KEY = "Idempotency-Key";
    public static final String STRIPE_SIGNATURE = "Stripe-Signature";

    private InvoiceConstants() {
    }
}
