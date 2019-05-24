/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

/**
 *
 * @author sylvia
 */
public enum QueryCEARTitle {
    
    UNPROCESSED("Unreviewed code enforcement action requests"),
    ATTACHED_TO_CECASE("Assigned to a code enforcement case"),
    ALL_TODAY("All action requests with activity today"),
    ALL_PAST7DAYS("All valid action requests created in the past 7 days"),
    ALL_PAST30("All valid action requests created in the past 30 days"),
    ALL_PASTYEAR("All valid action requests created in the past year");
    
    private final String label;
    
    private QueryCEARTitle(String l){
        this.label = l;
    }
    
    public String getLabel(){
        return label;
    }
    
    
}
