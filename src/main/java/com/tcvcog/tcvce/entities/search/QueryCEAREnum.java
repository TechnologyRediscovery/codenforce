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
public  enum        QueryCEAREnum 
        implements  IFace_RankLowerBounded{
    
    UNPROCESSED(        "Needs review by officer", 
                        "Code enforcement action requests that have not been reviewed", 
                        RoleType.MuniReader, 
                        true),
    
    ATTACHED_TO_CECASE( "Assigned to a case", 
                        "Code enforcement action requests from the past 30 days that was either used to create a new case or was attached to an existing code enforcement case", 
                        RoleType.MuniReader, 
                        true),
    
    ALL_TODAY(          "Requests today" , 
                        "All action requests with activity today", 
                        RoleType.MuniReader, 
                        true),
    
    ALL_PAST7DAYS(      "Requests past 7 days" , 
                        "All valid action requests created in the past 7 days", 
                        RoleType.MuniReader, 
                        true),
    
    ALL_PAST30(         "Requests past 30 days", 
                        "All valid action requests created in the past 30 days", 
                        RoleType.MuniReader, 
                        true),
    
    ALL_PASTYEAR(       "Requests past 365 days", 
                        "All valid action requests created in the past year", 
                        RoleType.MuniReader, 
                        true),
    
    BY_CURRENT_USER(    "***Under construction***", 
                        "Action requests attributed to the current user", 
                        RoleType.MuniReader, 
                        true),
    
    CUSTOM(             "Custom configuration", 
                        "Results based on the injected search parameters", 
                        RoleType.MuniReader, 
                        true);
    
    private final String title;
    private final String desc;
    private final RoleType requiredRoleMin;
    private final boolean log;
    
    private QueryCEAREnum(String t, String l, RoleType minRoleType, boolean lg){
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
