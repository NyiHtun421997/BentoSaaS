package com.nyihtuun.bentosystem.planmanagementservice.application_service.ports.input.service;

import com.nyihtuun.bentosystem.planmanagementservice.domain.service.PeriodContext;
import de.focus_shift.jollyday.core.HolidayCalendar;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Component
public class BusinessCalendarServiceImpl implements BusinessCalendarService {
    private final HolidayManager manager;

    public BusinessCalendarServiceImpl(HolidayManager manager) {
        this.manager = manager;
    }

    public PeriodContext getCurrentMonthBusinessPeriod(YearMonth yearMonth) {
        List<LocalDate> businessDays = yearMonth.atDay(1)
                                        .datesUntil(yearMonth.atEndOfMonth().plusDays(1))
                                        .filter(date -> !manager.isHoliday(date))
                                        .filter(date -> !isWeekend(date))
                                        .toList();

        return new PeriodContext(yearMonth.atDay(1), yearMonth.atEndOfMonth(), businessDays);

    }
}
