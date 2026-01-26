package com.nyihtuun.bentosystem.invoiceservice.application_service;

public class InvoiceConstants {
    public static final String INVOICE_ITEM_PROCESSOR = "InvoiceItemProcessor";
    public static final String INVOICE_ITEM_READER = "InvoiceItemReader";

    public static final String INVOICE_SCHEDULER_CRON = "${invoice.scheduler-cron}";
    public static final String INVOICE_ZONE = "${invoice.zone}";
    public static final String PLAN_MANAGEMENT_SERVICE_ADDRESS = "${plan-management.service.address}";
    public static final String PLAN_MANAGEMENT_SERVICE_PORT = "${plan-management.service.port}";
    public static final String SUBSCRIPTION_SERVICE_ADDRESS = "${subscription.service.address}";
    public static final String SUBSCRIPTION_SERVICE_PORT = "${subscription.service.port}";

    private InvoiceConstants() {
    }
}
