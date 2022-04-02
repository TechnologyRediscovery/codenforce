/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.RoleType;



/**
 * Pre-built queries against city/state/zip
 * @author Ellen Bascomb of Apartment 31Y
 */
public enum QueryMailingCityStateZipEnum 
implements IFace_RankLowerBounded{
    ALL_RECORDS_BY_ZIPCODE         (   "Search by ZIP code",
                                    "Returns all record types for a gizen ZIP Code",
                                    RoleType.MuniStaff,
                                    false),
    
    ZIPCODES_BY_CITY_AND_STATE         (   "Search by city and state",
                                    "Returns zip codes associated with a city and state based on a case insensitive search of city",
                                    RoleType.Public,
                                    false),
    
    
    VALID_RECORDS_BY_ZIPCODE   (   "Search by ZIP code (Valid records only)",
                                    "Returns only valid record types for a gizen ZIP Code",
                                    RoleType.Public,
                                    false),
    
     DEFAULT_RECORD_BY_ZIPCODE            ("Search by ZIP code (Default city only)",
                                    "Returns only a single record, the default city & state for a gizen ZIP Code",
                                    RoleType.Public,
                                    false);
    
    private final String title;
    private final String desc;
    private final RoleType requiredRoleFloor;
    private final boolean log;
    
    
    private QueryMailingCityStateZipEnum(String t, String l, RoleType mrt, boolean lg){
        this.desc = l;
        this.title = t;
        this.log = lg;
        if(mrt != null){
            requiredRoleFloor = mrt;
        } else {
            requiredRoleFloor = RoleType.MuniReader;
        }
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
        return requiredRoleFloor;
    }

   
}
