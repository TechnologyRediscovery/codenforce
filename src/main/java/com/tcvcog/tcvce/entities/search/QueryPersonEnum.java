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
    
    PERSON_NAME(        "Name",
                        "Searches through name field only; case insensitive",
                        RoleType.MuniStaff,
                        true),
    
    PHONE(              "Phone",
                        "Searches through phone number field only; case insensitive",
                        RoleType.MuniStaff,
                        true),
    
    EMAIL(              "Email address",
                        "Searches through email field only; case insensitive",
                        RoleType.MuniStaff,
                        true),
    JOB_TITLE (         "Job title",
                        "Searches through job title field only; case insensitive",
                        RoleType.MuniStaff,
                        true),
    
    MINORS_ONLY(        "All persons under 18",
                        "Searches for all persons marked as under 18",
                        RoleType.MuniStaff,
                        true),
    
    
    BUSINESSES(        "All businesses",
                        "Returns all records flagged as a business entity",
                        RoleType.MuniStaff,
                        true),
    
    
    MULIT_HUMANS(        "All Multi-humans",
                        "Returns all records flagged as representing more than one human",
                        RoleType.MuniStaff,
                        true),
    
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