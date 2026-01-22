package com.nyihtuun.bentosystem.planmanagementservice.configuration;

import com.nyihtuun.bentosystem.planmanagementservice.domain.service.PlanManagementDomainService;
import com.nyihtuun.bentosystem.planmanagementservice.domain.service.PlanManagementDomainServiceImpl;
import de.focus_shift.jollyday.core.HolidayCalendar;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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
}
