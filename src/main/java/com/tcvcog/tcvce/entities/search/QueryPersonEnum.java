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
public enum QueryPersonEnum {
    
    ACTIVE_PERSONS("Active persons", "Persons associated with active code enf and occ cases", false, RoleType.MuniReader),
    USER_PERSONS("User persons", "Persons whose role is to store a system User's personal data, such as name and phone", false, RoleType.MuniReader),
    CUSTOM("Custom", "Custom", false, RoleType.MuniReader);
    
    
    private final String title;
    private final String desc;
    private final RoleType requiredRoleMin;
    private final boolean log;
    
    private QueryPersonEnum(String t, String l, boolean lg, RoleType rt){
        this.title = t;
        this.desc = l;
        this.log = lg;
        this.requiredRoleMin = rt;
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
    public RoleType getRequiredRoleMin() {
        return requiredRoleMin;
    }
    
    
}
