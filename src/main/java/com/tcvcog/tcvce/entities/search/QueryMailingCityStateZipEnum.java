/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.RoleType;



/**
 * Pre-built quries against city/state/zip
 * @author Ellen Bascomb of Apartment 31Y
 */
public enum QueryMailingCityStateZipEnum 
implements IFace_RankLowerBounded{
    ZIPCODES_BY_CITY_AND_STATE         (   "Zip codes in a given city and state",
                                    "Returns zip codes associated with a city and state based on a case insensitive search of city",
                                    RoleType.Public,
                                    false),
    
    ALL_RECORDS_BY_ZIPCODE         (   "All records assocaited by zip",
                                    "Returns all record types for a gizen ZIP Code",
                                    RoleType.MuniStaff,
                                    false),
    
    VALID_RECORDS_BY_ZIPCODE   (   "All valid records assocaited by zip",
                                    "Returns only valid record types for a gizen ZIP Code",
                                    RoleType.Public,
                                    false),
    
     DEFAULT_RECORD_BY_ZIPCODE            (   "Default city & state by zip",
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the requiredRoleFloor
     */
    public RoleType getRequiredRoleFloor() {
        return requiredRoleFloor;
    }
    
}
