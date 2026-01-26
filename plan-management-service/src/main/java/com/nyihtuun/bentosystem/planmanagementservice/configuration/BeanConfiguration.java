package com.nyihtuun.bentosystem.planmanagementservice.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nyihtuun.bentosystem.planmanagementservice.domain.service.PlanManagementDomainService;
import com.nyihtuun.bentosystem.planmanagementservice.domain.service.PlanManagementDomainServiceImpl;
import de.focus_shift.jollyday.core.HolidayCalendar;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties(PlanManagementConfigData.class)
public class BeanConfiguration {

    @Bean
    public PlanManagementDomainService planManagementDomainService() {
        return new PlanManagementDomainServiceImpl();
    }

    @Bean
    public HolidayManager holidayManager() {
        // This requires the 'jollyday-jaxb' implementation on the classpath.
        return HolidayManager.getInstance(ManagerParameters.create(HolidayCalendar.JAPAN));
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
