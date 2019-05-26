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
public enum QueryCEAREnum {
    
    UNPROCESSED("Needs review by officer", "Code enforcement action requests that have not been reviewed", 2, false),
    ATTACHED_TO_CECASE("Assigned to a case", "Code enforcement action requests from the past 30 days that was either used to create a new case or was attached to an existing code enforcement case", 2, false),
    ALL_TODAY("Requests today" , "All action requests with activity today", 2, false),
    ALL_PAST7DAYS("Requests past 7 days" , "All valid action requests created in the past 7 days", 2, false),
    ALL_PAST30("Requests past 30 days", "All valid action requests created in the past 30 days", 2, false),
    ALL_PASTYEAR("Requests past 365 days", "All valid action requests created in the past year", 2, false),
    BY_CURRENT_USER("***Under construction***", "Action requests attributed to the current user", 2, false),
    CUSTOM("Custom configuration", "Results based on the injected search parameters", 2, true);
    
    private final String title;
    private final String desc;
    private final int userRankMinimum;
    private final boolean log;
    
    private QueryCEAREnum(String t, String l, int rnkMin, boolean lg){
        this.desc = l;
        this.title = t;
        this.userRankMinimum = rnkMin;
        this.log = lg;
    }
    
    public String getDesc(){
        return desc;
    }
    
    public String getTitle(){
        return title;
    }

    /**
     * @return the userRankMinimum
     */
    public int getUserRankMinimum() {
        return userRankMinimum;
    }
    
    
    public boolean logQueryRun(){
        return log;
    }
    
    
}
