package com.tcvcog.tcvce.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeUtil {
    private final int DAYS_IN_YEAR = 365;

    /**
     * Converts a date from LocalDateTime to a string using DateTimeFormatter, returns null if
     * input is null.
     *
     * @param input
     * @return
     */
    public static String getPrettyDate(LocalDateTime input) {
        String formattedDateTime = "";
        if(input != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE dd MMM yyyy, HH:mm");
            formattedDateTime = input.format(formatter);
        }
        return formattedDateTime;
    }

    /**
     * Converts a date from LocalDateTime to a string using DateTimeFormatter while omitting the time, returns null if
     * input is null.
     *
     * @param input
     * @return
     */
    public static String getPrettyDateNoTime(LocalDateTime input) {
        String formattedDateTime = "";
        if(input != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE dd MMM yyyy");
            formattedDateTime = input.format(formatter);
        }
        return formattedDateTime;
    }


    /**
     * Converts a date from LocalDateTime to java.util.Date, returns null if
     * input is null.
     *
     * @param input
     * @return
     */
    public static java.util.Date convertUtilDate(LocalDateTime input) {
        Date utilDate = null;
        if (input != null) {
            utilDate = Date.from(input.atZone(ZoneId.systemDefault()).toInstant());
        }
        return utilDate;
    }

    /**
     * Converts a date from java.util.Date to LocalDateTime, returns null if
     * input is null.
     *
     * @param input
     * @return
     */
    public static LocalDateTime convertUtilDate(java.util.Date input) {
        LocalDateTime dateTime = null;
        if (input != null) {
            dateTime = input.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        return dateTime;
    }

    /**
     * Counts days between two LocalDateTimes and returns the primitive
     * @param from
     * @param to
     * @return
     */
    public static long getTimePeriodAsDays(LocalDateTime from, LocalDateTime to){
        if(from == null || to == null){
            return 0;
        }
        LocalDate dStart = from.toLocalDate();
        LocalDate dEnd = to.toLocalDate();
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(dStart, dEnd);

//
//        days = java.time.Period.between(from.toLocalDate(), to.toLocalDate()).getDays();
//        java.time.Period.between(LocalDate.MIN, LocalDate.MAX)
//        years = java.time.Period.between(from.toLocalDate(), to.toLocalDate()).getYears();
//        totalDays = days + (years * DAYS_IN_YEAR);
        return daysBetween ;
    }
}
