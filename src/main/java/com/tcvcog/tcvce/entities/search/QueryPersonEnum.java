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
public  enum        QueryPersonEnum 
        implements  IFace_RankLowerBounded{
    
    PERSON_NAME(        "First and or last name",
                        "Filters by first or last name parts; case insensitive",
                        RoleType.MuniStaff,
                        true),
    
    ACTIVE_PERSONS(     "Active persons", 
                        "Persons associated with active code enf and occ cases", 
                        RoleType.MuniReader,
                        true),
    
    USER_PERSONS(       "User persons", 
                        "Persons whose role is to store a system User's personal data, such as name and phone", 
                        RoleType.MuniReader,
                        true),
    
    TENANTS(            "Tenants",
                        "Persons who are attached to properties with type Rental and are type Tenant",
                        RoleType.MuniStaff,
                        true),
    
    PROPERTY_PERSONS(   "Property persons",
                        "All Person entities associated with the property in any way",
                        RoleType.MuniStaff,
                        true),
    
    OCCPERIOD_PERSONS(  "OccPeriod Persons",
                        "All persons associated with a given Occupancy Period",
                        RoleType.MuniStaff,
                        false),   
    
    CUSTOM(             "Custom", 
                        "Custom", 
                        RoleType.MuniReader,
                        true);
    
    private final String title;
    private final String desc;
    private final RoleType requiredRoleMin;
    private final boolean log;
    
    private QueryPersonEnum(String t, String l, RoleType minRoleType, boolean lg){
        
        this.title = t;
        this.desc = l;
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

    /**
     * @return the requiredRoleMin
     */
    @Override
    public RoleType getRequiredRoleMin() {
        return requiredRoleMin;
    }
    
    
}