/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.EntityUtils;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;

/**
 *
 * @author sylvia
 */
public class BOBQuery extends EntityUtils implements Serializable{
    
    private String queryTitle;
    private Municipality muni;
    private RoleType userRankAccessCeiling;

    public BOBQuery(String queryTitle, Municipality muni, User u) {
        this.queryTitle = queryTitle;
        this.muni = muni;
        
        
        
    }

    /**
     * @return the queryTitle
     */
    public String getQueryTitle() {
        return queryTitle;
    }

    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    

    /**
     * @param queryTitle the queryTitle to set
     */
    public void setQueryTitle(String queryTitle) {
        this.queryTitle = queryTitle;
    }

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @return the userRankAccessCeiling
     */
    public RoleType getUserRankAccessCeiling() {
        return userRankAccessCeiling;
    }

    /**
     * @param userRankAccessCeiling the userRankAccessCeiling to set
     */
    public void setUserRankAccessCeiling(RoleType userRankAccessCeiling) {
        this.userRankAccessCeiling = userRankAccessCeiling;
    }

    
   
    
    
}
