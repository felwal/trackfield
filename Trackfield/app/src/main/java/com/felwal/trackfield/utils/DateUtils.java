package com.felwal.trackfield.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public final class DateUtils {

    private DateUtils() {
        // this utility class is not publicly instantiable
    }

    //

    public static ChronoField toChronoField(ChronoUnit unit) {
        switch (unit) {
            case WEEKS:
                return ChronoField.ALIGNED_WEEK_OF_YEAR;
            case YEARS:
                return ChronoField.YEAR;
            case DAYS:
            default:
                return ChronoField.DAY_OF_MONTH;
        }
    }

    // truncate

    public static LocalDateTime dateTime(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.of(0, 0));
    }

    public static LocalDateTime truncateSecs(LocalDateTime dateTime) {
        return dateTime.truncatedTo(ChronoUnit.MINUTES);
    }

    // epoch

    public static LocalDateTime ofEpochSecond(long seconds) {
        return LocalDateTime.ofEpochSecond(seconds, 0, ZoneOffset.UTC);
    }

    public static long toEpochSecond(LocalDateTime dateTime) {
        // = Unix
        return dateTime.atZone(ZoneId.of("UTC")).toEpochSecond();
    }

    // compare

    public static LocalDateTime first(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
    }

    public static LocalDateTime last(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    // convert

    public static LocalDateTime atStartOfWeek(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1).atStartOfDay();
    }

    public static LocalDateTime atEndOfWeek(LocalDate date) {
        return date.plusDays(7 - date.getDayOfWeek().getValue()).atTime(23, 59, 59);
    }

    public static LocalDateTime atStartOfMonth(LocalDate date) {
        return date.minusDays(date.getDayOfMonth() - 1).atStartOfDay();
    }

    public static LocalDateTime atEndOfMonth(LocalDate date) {
        return date.plusDays(date.lengthOfMonth() - date.getDayOfMonth()).atTime(23, 59, 59);
    }

    public static LocalDateTime atStartOfYear(LocalDate date) {
        return date.minusDays(date.getDayOfYear() - 1).atStartOfDay();
    }

    public static LocalDateTime atEndOfYear(LocalDate date) {
        return date.plusDays(date.lengthOfYear() - date.getDayOfYear()).atTime(23, 59, 59);
    }

}
