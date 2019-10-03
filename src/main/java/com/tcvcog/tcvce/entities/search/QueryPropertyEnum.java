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
public enum QueryPropertyEnum {
    
    OPENCECASES_OCCPERIODSINPROCESS("Active properties", "Properties with open code enf cases and occupancy period authorization in process", 2, false),
    CUSTOM("Custom", "Custom", 2, false);
    
    private final String title;
    private final String desc;
    private final int userRankMinimum;
    private final boolean log;
    
    private QueryPropertyEnum(String t, String l, int rnkMin, boolean lg){
        this.title = t;
        this.desc = l;
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
