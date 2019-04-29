/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Superlcass of entity objects: a Hodgepodge set of methods for use by 
 * entities, such as returning a nicely
 * formatted String version of any given LocalDateTime 
 * @author sylvia Baskem
 */
public class EntityUtils {
    
    private final int DAYS_IN_YEAR = 365;
    
    protected String getPrettyDate(LocalDateTime ldtDate){
        String formattedDateTime = "";
        if(ldtDate != null){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE dd MMM yyyy, HH:mm");
            formattedDateTime = ldtDate.format(formatter); 
            
        }
        return formattedDateTime;
    }
   
    
    /**
     * Counts days between two LocalDateTimes and returns the primitive
     * @param from
     * @param to
     * @return 
     */
    public long getTimePeriodAsDays(LocalDateTime from, LocalDateTime to){
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
