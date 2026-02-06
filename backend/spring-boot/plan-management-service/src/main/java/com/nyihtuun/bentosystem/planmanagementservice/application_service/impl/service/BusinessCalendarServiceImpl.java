package com.nyihtuun.bentosystem.planmanagementservice.application_service.impl.service;

import com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service.BusinessCalendarService;
import com.nyihtuun.bentosystem.planmanagementservice.domain.service.PeriodContext;
import de.focus_shift.jollyday.core.HolidayManager;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Component
public class BusinessCalendarServiceImpl implements BusinessCalendarService {
    private final HolidayManager manager;

    public BusinessCalendarServiceImpl(HolidayManager manager) {
        this.manager = manager;
    }

    @Override
    public PeriodContext getCurrentMonthBusinessPeriod(YearMonth yearMonth) {
        List<LocalDate> businessDays = yearMonth.atDay(1)
                                        .datesUntil(yearMonth.atEndOfMonth().plusDays(1))
                                        .filter(date -> !manager.isHoliday(date))
                                        .filter(date -> !isWeekend(date))
                                        .toList();

        return new PeriodContext(yearMonth.atDay(1), yearMonth.atEndOfMonth(), businessDays);

    }

    @Override
    public boolean isUpdatableDate(LocalDate localDate) {
        return localDate.isBefore(localDate.with(TemporalAdjusters.lastDayOfMonth()).minusDays(7));
    }
}
