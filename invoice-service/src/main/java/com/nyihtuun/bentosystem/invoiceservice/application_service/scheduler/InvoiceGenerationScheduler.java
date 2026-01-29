package com.nyihtuun.bentosystem.invoiceservice.application_service.scheduler;

import com.nyihtuun.bentosystem.invoiceservice.configuration.InvoiceConfigData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.ZoneOffset;

import static com.nyihtuun.bentosystem.invoiceservice.InvoiceConstants.INVOICE_SCHEDULER_CRON;
import static com.nyihtuun.bentosystem.invoiceservice.InvoiceConstants.INVOICE_ZONE;

@Slf4j
@Component
public class InvoiceGenerationScheduler {

    private final JobOperator jobOperator;
    private final InvoiceConfigData configData;
    private final Job issueInvoiceJob;

    @Autowired
    public InvoiceGenerationScheduler(JobOperator jobOperator, InvoiceConfigData configData, Job issueInvoiceJob) {
        this.jobOperator = jobOperator;
        this.configData = configData;
        this.issueInvoiceJob = issueInvoiceJob;
    }

    @Scheduled(cron = INVOICE_SCHEDULER_CRON, zone = INVOICE_ZONE)
    public void process() throws JobInstanceAlreadyCompleteException, InvalidJobParametersException, JobExecutionAlreadyRunningException, JobRestartException {
        log.info("Invoice generation task started.");

        String billingYearMonth = YearMonth.now(ZoneOffset.UTC).plusMonths(1).toString();

        JobParameters jobParameters = new JobParametersBuilder()
                .addString(configData.jobParamIssuedAt(), billingYearMonth)
                .addLong(configData.jobParamRunId(), System.currentTimeMillis())
                .toJobParameters();

        jobOperator.start(issueInvoiceJob, jobParameters);
        log.info("Invoice generation task finished.");
    }
}
