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
    
    public String getPrettyDate(LocalDateTime ldt){
        String formattedDateTime = null;
        if(ldt != null){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE dd MMM yyyy, HH:mm");
            formattedDateTime = ldt.format(formatter); 
            
        }
        return formattedDateTime;
    }
    
    public int getTimePeriodAsDays(LocalDateTime from, LocalDateTime to){
        int totalDays;
        int days;
        int years;
        days = java.time.Period.between(from.toLocalDate(), to.toLocalDate()).getDays();
        years = java.time.Period.between(from.toLocalDate(), to.toLocalDate()).getYears();
        totalDays = days + (years * DAYS_IN_YEAR);
        return totalDays;
    }
    
}
