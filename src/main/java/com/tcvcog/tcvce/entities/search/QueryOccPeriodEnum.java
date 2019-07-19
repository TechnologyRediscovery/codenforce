/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

/**
 *
 * @author sylvia
 */
public enum QueryOccPeriodEnum {
    
    AUTHWORKINPROGRESS("Occpancy authorization underway, All Phases", "Units with occ periods which do not yet have a certified authorization stamp", 2, false),
    RENTAL_ALL("Current rental units - All statuses", "Units with occ periods whose type is rental compatible regardless of period authorization", 2, false),
    RENTAL_UNREGISTERED("Current rental units - Incomplete registration", "Units with occupancy periods declared as requiring registration but whose registration is currently incomplete", 2, false),
    RENTAL_REGISTERED("Current rental units - Complete registration", "Units whose current occupancy period requires registration and whose registration is complete ", 2, false);
    
    private final String title;
    private final String desc;
    private final int userRankMinimum;
    private final boolean log;
    
    private QueryOccPeriodEnum(String t, String l, int rnkMin, boolean lg){
        this.desc = l;
        this.title = t;
        this.userRankMinimum = rnkMin;
        this.log = lg;
    }
    
    public String getDesc(){
        return desc;
    }
    
    public String getTitle(){
        return title;
    }

    /**
     * @return the userRankMinimum
     */
    public int getUserRankMinimum() {
        return userRankMinimum;
    }
    
    
    public boolean logQueryRun(){
        return log;
    }
    
    
}
