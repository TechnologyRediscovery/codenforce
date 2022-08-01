/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.session.entities;

import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventCnFPropUnitCasePeriodHeavy;
import com.tcvcog.tcvce.util.DateTimeUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Wrapper of a list of events and the day those events pertain to
 * @author Ellen Basomb (apartment 31Y)
 */
public class EventCalendarDay extends DateTimeUtil {
    private String dayPrettyPrefix;
    private LocalDate day;
    private LocalDateTime ldtLowerBound;
    private LocalDateTime ldtUpperBound;
    private List<EventCnFPropUnitCasePeriodHeavy> evList;

    /**
     * @return the day
     */
    public LocalDate getDay() {
        return day;
    }

    /**
     * @return the evList
     */
    public List<EventCnFPropUnitCasePeriodHeavy> getEvList() {
        return evList;
    }

    /**
     * @param day the day to set
     */
    public void setDay(LocalDate day) {
        this.day = day;
    }

    /**
     * @param evList the evList to set
     */
    public void setEvList(List<EventCnFPropUnitCasePeriodHeavy> evList) {
        this.evList = evList;
    }

    /**
     * @return the dayPrettyPrefix
     */
    public String getDayPrettyPrefix() {
        return dayPrettyPrefix;
    }

    /**
     * @param dayPrettyPrefix the dayPrettyPrefix to set
     */
    public void setDayPrettyPrefix(String dayPrettyPrefix) {
        this.dayPrettyPrefix = dayPrettyPrefix;
    }

    /**
     * @return the ldtLowerBound
     */
    public LocalDateTime getLdtLowerBound() {
        return ldtLowerBound;
    }

    /**
     * @return the ldtUpperBound
     */
    public LocalDateTime getLdtUpperBound() {
        return ldtUpperBound;
    }

    /**
     * @param ldtLowerBound the ldtLowerBound to set
     */
    public void setLdtLowerBound(LocalDateTime ldtLowerBound) {
        this.ldtLowerBound = ldtLowerBound;
    }

    /**
     * @param ldtUpperBound the ldtUpperBound to set
     */
    public void setLdtUpperBound(LocalDateTime ldtUpperBound) {
        this.ldtUpperBound = ldtUpperBound;
    }
    
    
}
