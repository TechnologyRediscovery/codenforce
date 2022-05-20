/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

/**
 * Enumerates the three possible uses for a text block on an occ permit
 * @author sylvia
 */
public enum TextBlockPermitFieldEnum {
    STIPULATIONS("Permit Stipulations"),
    NOTICES("Permit Notices"),
    COMMENTS("Permit Comments");
    
    private final String label;
    
    private TextBlockPermitFieldEnum(String l){
        label = l;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }
    
}
