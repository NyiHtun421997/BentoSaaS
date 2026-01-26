package com.nyihtuun.bentosystem.invoiceservice.application_service.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchJobExecutionListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Batch job execution started: {} at: {}", jobExecution.getJobInstance().getJobName(), jobExecution.getStartTime());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("Batch job execution completed: {} at: {}", jobExecution.getJobInstance().getJobName(), jobExecution.getEndTime());
    }
}
