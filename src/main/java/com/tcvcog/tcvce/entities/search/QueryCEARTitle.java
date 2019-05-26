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
    
    UNPROCESSED("Needs review by officer", "Code enforcement action requests that have not been reviewed"),
    ATTACHED_TO_CECASE("Assigned to a case", "Code enforcemetn action request "
            + "that was either used to create a new case or was attached to an existing code enforcement case"),
    ALL_TODAY("Requests today" , "All action requests with activity today"),
    ALL_PAST7DAYS("Requests thsi week" , "All valid action requests created in the past 7 days"),
    ALL_PAST30("Requests this month", "All valid action requests created in the past 30 days"),
    ALL_PASTYEAR("Requests past year", "All valid action requests created in the past year"),
    BY_CURRENT_USER("Requestor is you", "Action requests attributed to the current user");
    
    private final String title;
    private final String desc;
    
    private QueryCEARTitle(String t, String l){
        this.desc = l;
        this.title = t;
    }
    
    public String getDesc(){
        return desc;
    }
    
    public String getTitle(){
        return title;
    }
    
    
}
