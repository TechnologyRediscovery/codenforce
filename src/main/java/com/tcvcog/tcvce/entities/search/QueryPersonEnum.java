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
public enum QueryPersonEnum {
    
    ACTIVE_PERSONS("Active persons", "Persons associated with active code enf and occ cases", 2, false);
    
    private final String title;
    private final String desc;
    private final int userRankMinimum;
    private final boolean log;
    
    private QueryPersonEnum(String t, String l, int rnkMin, boolean lg){
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
