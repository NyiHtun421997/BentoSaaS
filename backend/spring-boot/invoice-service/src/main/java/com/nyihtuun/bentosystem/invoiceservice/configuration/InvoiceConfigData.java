package com.nyihtuun.bentosystem.invoiceservice.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "invoice")
public record InvoiceConfigData(
        int batchChunkSize,
        String stepName,
        String jobName,
        String jobParamIssuedAt,
        String jobParamRunId,
        String schedulerCron,
        String zone
) { }
