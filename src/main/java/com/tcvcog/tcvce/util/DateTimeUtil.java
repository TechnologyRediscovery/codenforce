package com.tcvcog.tcvce.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeUtil {
    private final int DAYS_IN_YEAR = 365;

    /**
     * Converts a date from LocalDateTime to a string using DateTimeFormatter, returns empty string if
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
     * Converts a local date to a no time string. 
     * @param input if null, empty string returned
     * @return String representing a date only
     */
    public static String getPrettyLocalDateNoTime(LocalDate input){
        String formattedDateTime = "";
        if(input != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE dd MMM yyyy");
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
