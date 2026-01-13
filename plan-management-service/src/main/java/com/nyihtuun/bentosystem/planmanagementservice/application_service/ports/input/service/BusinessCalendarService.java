package com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service;

import com.nyihtuun.bentosystem.planmanagementservice.domain.service.PeriodContext;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

public interface BusinessCalendarService {
    PeriodContext getCurrentMonthBusinessPeriod(YearMonth yearMonth);

    default boolean isWeekend(LocalDate date) {
        return (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);
    }
}
