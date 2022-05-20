/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

/**
 * Enumerates the four fields on an occ permit that can hold persons
 * @author sylvia
 */
public enum OccPermitPersonListEnum {
    CURRENT_OWNER("Current property owner(s)"),
    NEW_OWNER("New owner/tenant(s)"),
    MANAGERS("Property manager(s)"),
    TENANTS("Tenant(s)");
    
    private final String label;
    
    private OccPermitPersonListEnum(String l){
        label = l;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }
}
