package com.nyihtuun.bentosystem.planmanagementservice.domain.service;

import com.nyihtuun.bentosystem.planmanagementservice.domain.entity.Plan;

import java.util.List;

public interface PlanDomainService {
    void generateSchedules(List<Plan> plans, PeriodContext periodContext);
}
