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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
public class SchedulerStartupRunner {

    private final InvoiceGenerationScheduler scheduler;

    @Autowired
    public SchedulerStartupRunner(InvoiceGenerationScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runOnceOnStartup() throws JobInstanceAlreadyCompleteException, InvalidJobParametersException, JobExecutionAlreadyRunningException, JobRestartException {
        scheduler.process();
    }
}
