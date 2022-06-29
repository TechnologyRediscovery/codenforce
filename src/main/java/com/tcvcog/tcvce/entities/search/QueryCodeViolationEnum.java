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
public  enum QueryCodeViolationEnum 
        implements IFace_RankLowerBounded{
    
   
    
    MUNI_ALL(               "All violations in all in current muni",
                            "Including violations with compliance achieved",
                            RoleType.MuniStaff,
                            true),
    
    LOGGED_PAST30_NOV_CITMAYBE    ("Logged on any case with NOV in past 30 days",
                            "",
                            RoleType.MuniStaff,
                            true),
    
    
    LOGGED_PAST7_NOV_CITMAYBE ("Logged on any case with NOV in past 7 days",
                            "",
                            RoleType.MuniStaff,
                            true),
    
    
    // IMPLEMENT ME
    COMPLIANCE_DUE_WIHTIN   ("Currently within compliance date",
                            "",
                            RoleType.MuniStaff,
                            true),
    
    
    // IMPLEMENT ME
    COMPLIANCE_DUE_EXPIRED  ("Expired compliance timeframe",
                            "",
                            RoleType.MuniStaff,
                            true),
    
    
    
    COMP_PAST30             ("Compliance achieved in past 30 days",
                            "",
                            RoleType.MuniStaff,
                            true),
    
    COMP_PAST7              ("Compliance achieved in past 7 days",
                            "",
                            RoleType.MuniStaff,
                            true),
    
    STIP_NEXT7              ("Compliance stipulated in upcoming 7 days",
                            "",
                            RoleType.MuniStaff,
                            true),
    
    STIP_NEXT30             ("Compliance stipulated in upcoming 30 days",
                            "",
                            RoleType.MuniStaff,
                            true),
    
    CITED_PAST30            ("Cited in past 30 days",
                            "",
                            RoleType.MuniStaff,
                            true),
    
    CITED_PAST7            ("Cited in the past 7 days",
                            "",
                            RoleType.MuniStaff,
                            true),
    
    LOGGED_IN_DATE_RANGE      ("Attached to a case during date range",
                            "Regardless of NOV and Citation status",
                            RoleType.MuniStaff,
                            true),
    
    LOGGED_NO_NOV_EVER      ("Attached to a case but not included in a notice EVER",
                            "Potentially violations attached to a case from a property inspection; there's a window",
                            RoleType.MuniStaff,
                            true),
    
    LOGGED_NO_NOV_PAST30    ("Attached to a case but not included in a notice in the past 30 days",
                            "",
                            RoleType.MuniStaff,
                            true),
    
    LOGGED_NO_NOV_PAST7     ("Attached to a case but not included in a notice in the past 7 days",
                            "",
                            RoleType.MuniStaff,
                            true),
    
    LOGGED_CITED_NONOV      ("AUDIT: Attached to a case and cited without a notice",
                            "",
                            RoleType.MuniStaff,
                            true),
    
    ALL_OUTSTANDING         ("All Unresolved Violations",
                            "",
                            RoleType.MuniStaff,
                            true)
    
    
    ;
    
    
    
    
    private final String title;
    private final String desc;
    private final RoleType requiredRoleMin;
    private final boolean log;
    
    private QueryCodeViolationEnum(String t, String l, RoleType minRoleType, boolean lg){
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
