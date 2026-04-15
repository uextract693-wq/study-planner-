package com.example.smartstudyplanner.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class DateUtil {
    private DateUtil() {
    }

    public static long getDaysBetween(LocalDate today, LocalDate deadline) {
        return ChronoUnit.DAYS.between(today, deadline);
    }
}
