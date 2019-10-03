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
public enum QueryCECaseEnum {
    
    OPENCASES("All open cases", "Code enforcement case containing any unresolved violations", 2, false),
    EXPIRED_TIMEFRAMES("Overdue cases", "Cases with one or more violations with expired compliance timeframes", 2, false),
    CURRENT_TIMEFRAMES("Within compliance timeframe", "Cases whose violations are all insdie compliance timeframes", 2, false),
    OPENED_30DAYS("Opened past 30 days", "Cases opene in the past 30 days", 2, false),
    CLOSED_30DAYS("Closed in the past 30 days", "Any case closed in the past 30 days", 2, false),
    UNRESOLVED_CITATIONS("Outstanding citations", "Cases with filed citations and are in court system with unpaid citations", 2, false),
    ANY_ACTIVITY_7Days("Any case activity in past week","Cases with any new events in the past 7 days", 2, false),
    ANY_ACTIVITY_30Days("Any case activyt in past month","Cases with any new events in the past 30 days", 2, false),
    CUSTOM("Custom case query", "Customized search parameters", 2, false);
    
    private final String title;
    private final String desc;
    private final int userRankMinimum;
    private final boolean log;
    
    private QueryCECaseEnum(String t, String l, int rnkMin, boolean lg){
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
