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
public  enum     QueryOccPermitEnum 
        implements IFace_RankLowerBounded{
    
    
    
    ALL_FINALIZED(          "All finalized permits", 
                            "Including nullified after finalization", 
                            RoleType.MuniReader, 
                            true),
    
    CUSTOM(                 "Custom", 
                            "Custom", 
                            RoleType.MuniReader, 
                            true);
    
    private final String title;
    private final String desc;
    private final RoleType requiredRoleMin;
    private final boolean log;
    
    private QueryOccPermitEnum(String t, String l, RoleType minRoleType, boolean lg){
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
