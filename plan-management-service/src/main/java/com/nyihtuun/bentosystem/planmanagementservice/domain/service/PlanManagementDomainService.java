package com.nyihtuun.bentosystem.planmanagementservice.domain.service;

import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Plan;


public interface PlanManagementDomainService {
    GenerateSchedulesResult generateSchedules(Plan plan, PeriodContext periodContext);
}
