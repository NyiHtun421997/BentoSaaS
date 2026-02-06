package com.nyihtuun.bentosystem.planmanagementservice.domain.service;

import java.time.LocalDate;
import java.util.List;

public record PeriodContext(LocalDate periodStart, LocalDate periodEnd, List<LocalDate> businessDaysOfCurrentMonth) {
}
