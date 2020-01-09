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
public  enum QueryCECaseEnum 
        implements IFace_RankLowerBounded{
    
    OPENCASES(              "All open cases", 
                            "Code enforcement case containing any unresolved violations", 
                            RoleType.MuniReader, 
                            true),
    
    EXPIRED_TIMEFRAMES(     "Overdue cases", 
                            "Cases with one or more violations with expired compliance timeframes", 
                            RoleType.MuniReader, 
                            true),
    
    CURRENT_TIMEFRAMES(     "Within compliance timeframe", 
                            "Cases whose violations are all insdie compliance timeframes", 
                            RoleType.MuniReader, 
                            true),
    
    OPENED_30DAYS(          "Opened past 30 days", 
                            "Cases opene in the past 30 days", 
                            RoleType.MuniReader, 
                            true),
    
    CLOSED_30DAYS(          "Closed in the past 30 days", 
                            "Any case closed in the past 30 days", 
                            RoleType.MuniReader, 
                            true),
    
    UNRESOLVED_CITATIONS(   "Outstanding citations", 
                            "Cases with filed citations and are in court system with unpaid citations", 
                            RoleType.MuniReader, 
                            true),
    
    ANY_ACTIVITY_7Days(     "Any case activity in past week",
                            "Cases with any new events in the past 7 days", 
                            RoleType.MuniReader, 
                            true),
    
    ANY_ACTIVITY_30Days(    "Any case activyt in past month",
                            "Cases with any new events in the past 30 days", 
                            RoleType.MuniReader, 
                            true),
    
    PROPERTY(               "CE cases by property",
                            "CE Cases attached to a single specified property", 
                            RoleType.MuniReader, 
                            true),
    
    CUSTOM(                 "Custom case query", 
                            "Customized search parameters", 
                            RoleType.MuniReader, 
                            true);
    
    private final String title;
    private final String desc;
    private final RoleType requiredRoleMin;
    private final boolean log;
    
    private QueryCECaseEnum(String t, String l, RoleType minRoleType, boolean lg){
        this.desc = l;
        this.title = t;
        this.log = lg;
        if(minRoleType != null){
            this.requiredRoleMin = minRoleType;
        } else {
            this.requiredRoleMin = RoleType.MuniStaff;
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
        return requiredRoleMin;
    }
    
    
}
