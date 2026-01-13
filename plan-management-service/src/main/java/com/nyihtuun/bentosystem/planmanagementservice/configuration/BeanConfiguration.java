package com.nyihtuun.bentosystem.planmanagementservice.configuration;

import com.nyihtuun.bentosystem.planmanagementservice.domain.service.PlanManagementDomainService;
import com.nyihtuun.bentosystem.planmanagementservice.domain.service.PlanManagementDomainServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public PlanManagementDomainService planManagementDomainService() {
        return new PlanManagementDomainServiceImpl();
    }
}
