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
public  enum        QueryPropertyEnum 
        implements  IFace_RankLowerBounded{
    
    HOUSESTREETNUM(                     "Search by address", 
                                        "Enter house and street number to search in your current municipality", 
                                        RoleType.MuniReader, 
                                        false),
    
    OPENCECASES_OCCPERIODSINPROCESS(    "Active properties", 
                                        "Properties with open code enf cases and occupancy period authorization in process", 
                                        RoleType.MuniReader, 
                                        false),
    
    PERSONS(                            "Properties connected to a given person",
                                        "Includes all Person roles",
                                        RoleType.MuniStaff,
                                        true),
    
    CUSTOM(                             "Custom", 
                                        "Custom", 
                                        RoleType.MuniReader, 
                                        false);
    
    private final String title;
    private final String desc;
    private final RoleType requiredRoleMin;
    private final boolean log;
    
    private QueryPropertyEnum(String t, String l, RoleType minRoleType, boolean lg){
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

    @Override
    public RoleType getRequiredRoleMin() {
        return requiredRoleMin;
    }
    
    
}
