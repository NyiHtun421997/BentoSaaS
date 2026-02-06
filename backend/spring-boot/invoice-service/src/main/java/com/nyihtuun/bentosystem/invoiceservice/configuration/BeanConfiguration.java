package com.nyihtuun.bentosystem.invoiceservice.configuration;

import com.nyihtuun.bentosystem.invoiceservice.InvoiceConstants;
import com.nyihtuun.bentosystem.invoiceservice.application_service.batch.BatchJobExecutionListener;
import com.nyihtuun.bentosystem.invoiceservice.application_service.batch.SubscriptionContext;
import com.nyihtuun.bentosystem.invoiceservice.data_access.jpa_entity.InvoiceEntity;
import com.nyihtuun.bentosystem.invoiceservice.data_access.jpa_repository.InvoiceJpaRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BeanConfiguration {

    @Bean
    @StepScope
    public RepositoryItemWriter<InvoiceEntity> invoiceItemWriter(InvoiceJpaRepository invoiceJpaRepository) {
        return new RepositoryItemWriterBuilder<InvoiceEntity>()
                .repository(invoiceJpaRepository)
                .build();
    }

    @Bean
    public Step step(JobRepository jobRepository,
                        PlatformTransactionManager transactionManager,
                        @Qualifier(InvoiceConstants.INVOICE_ITEM_PROCESSOR) ItemProcessor<SubscriptionContext, InvoiceEntity> invoiceItemProcessor,
                        @Qualifier(InvoiceConstants.INVOICE_ITEM_READER) ItemReader<SubscriptionContext> invoiceItemReader,
                        RepositoryItemWriter<InvoiceEntity> invoiceItemWriter,
                        InvoiceConfigData invoiceConfigData) {
        return new StepBuilder(invoiceConfigData.stepName(), jobRepository)
                .<SubscriptionContext, InvoiceEntity>chunk(invoiceConfigData.batchChunkSize())
                .transactionManager(transactionManager)
                .reader(invoiceItemReader)
                .processor(invoiceItemProcessor)
                .writer(invoiceItemWriter)
                .build();
    }

    @Bean
    public Job job(JobRepository jobRepository, Step step,
                   InvoiceConfigData invoiceConfigData,
                   BatchJobExecutionListener batchJobExecutionListener) {
        return new JobBuilder(invoiceConfigData.jobName(), jobRepository)
                .start(step)
                .listener(batchJobExecutionListener)
                .build();
    }
}
