/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.RoleType;

/**
 *
 * @author sylvia
 */
public  enum     QueryOccPeriodEnum 
        implements IFace_RankLowerBounded{
    
    ALL_PERIODS_IN_MUNI(    "All occupancy periods in muni", 
                            "All periods", 
                            RoleType.MuniReader, 
                            true),
    
    AUTHWORKINPROGRESS(     "Occpancy authorization underway, All Phases", 
                            "Units with occ periods which do not yet have a certified authorization stamp", 
                            RoleType.MuniReader, 
                            true),
    
    RENTAL_ALL(             "Current rental units - All statuses", 
                            "Units with occ periods whose type is rental compatible regardless of period authorization", 
                            RoleType.MuniReader, 
                            true),
    
    RENTAL_UNREGISTERED(    "Current rental units - Incomplete registration", 
                            "Units with occupancy periods declared as requiring registration but whose registration is currently incomplete", 
                            RoleType.MuniReader, 
                            false),
    
    RENTAL_REGISTERED(      "Current rental units - Complete registration", 
                            "Units whose current occupancy period requires registration and whose registration is complete ", 
                            RoleType.MuniReader, 
                            false),
    
    PERSONS(                "Occ periods associated with a given Person",
                            "Must be linked somehow to the OccPeriod directly",
                            RoleType.MuniReader,
                            false),
    
    CUSTOM(                 "Custom", 
                            "Custom", 
                            RoleType.MuniReader, 
                            true);
    
    private final String title;
    private final String desc;
    private final RoleType requiredRoleMin;
    private final boolean log;
    
    private QueryOccPeriodEnum(String t, String l, RoleType minRoleType, boolean lg){
        this.desc = l;
        this.title = t;
         if(minRoleType != null){
            this.requiredRoleMin = minRoleType;
        } else {
            this.requiredRoleMin = RoleType.MuniStaff;
        }
        this.log = lg;
    }
    
    public String getDesc(){
        return desc;
    }
    
    public String getTitle(){
        return title;
    }

    
    public boolean logQueryRun(){
        return log;
    }

    @Override
    public RoleType getRequiredRoleMin() {
        return requiredRoleMin;
    }
    
    
}
