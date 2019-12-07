/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

/**
 * Superlcass of entity objects: a Hodgepodge set of methods for use by 
 * entities, such as returning a nicely
 * formatted String version of any given LocalDateTime 
 * @author sylvia Baskem
 */
public class EntityUtils {
    
    private final int DAYS_IN_YEAR = 365;
    
    public static String getPrettyDate(LocalDateTime ldtDate){
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
    public static long getTimePeriodAsDays(LocalDateTime from, LocalDateTime to){
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
    
    /**
     * Pretty prints a List of Integers
     * Used by CodeViolations to list their citations and notices
     * @param intList
     * @return 
     */
    public static String fomatIDListAsString(List<Integer> intList){
        
        String listString;
        StringBuilder sb = new StringBuilder();
        Iterator<Integer> it;
        
        if(!intList.isEmpty()){
            sb.append("ID #s: ");
            it = intList.iterator();
            while(it.hasNext()){
                Integer i = it.next();
                sb.append(String.valueOf(i));
                if(it.hasNext()){
                    sb.append(", ");
                }
            }
             listString = sb.toString();
        } else listString = "";
        
        return listString;
    }
}
